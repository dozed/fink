#
# Copyright (C) 2009-2011 the original author or authors.
# See the notice.md file distributed with this work for additional
# information regarding copyright ownership.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

define [
  "frameworks/order!frameworks/jquery"
  "frameworks/order!frameworks/underscore-1.1.7"
  "frameworks/order!frameworks/underscore.string-1.1.5"
  "frameworks/order!frameworks/backbone-0.5.3"
  "frameworks/order!frameworks/backbone.modelbinding"
  "frameworks/order!frameworks/jquery.flot"
  "frameworks/order!frameworks/jquery.flot.stack"
  "frameworks/order!frameworks/jquery.flot.crosshair"
  "frameworks/order!frameworks/jquery.json-2.2"
  "frameworks/order!frameworks/jquery.effects.core"
  "frameworks/order!frameworks/jquery.effects.slide"
  "frameworks/order!frameworks/jquery.datatables"
  "frameworks/order!frameworks/jquery.tagsinput"
  "frameworks/order!frameworks/jquery.ui.min"
  "frameworks/order!frameworks/jquery.tagit"
  "frameworks/order!frameworks/coffeejade-runtime"
  "frameworks/order!frameworks/bootstrap-dropdown"
  "frameworks/order!frameworks/bootstrap-twipsy"
  "frameworks/order!frameworks/bootstrap-popover"
  "frameworks/order!frameworks/bootstrap-modal"
], ->
    
  window.lazy = (cache_field, func) ->
    ->
      this["_" + cache_field] = func.apply(this, arguments)  unless this["_" + cache_field]
      this["_" + cache_field]

  ClassHelpers = {}
  
  _.find = _.detect
  
  # Wrap an optional error callback with a fallback error event.
  wrapError = (onError, model, options) ->
    (resp) ->
      if onError
        onError(model, resp, options)
      else 
        model.trigger('error', model, resp, options)
  
  Model = Backbone.Model.extend(ClassHelpers).extend
    property: (field)-> 
      rc = =>
        if arguments.length >0
          atts = {}
          atts[field] = arguments[0]
          @set atts
        @get field
      rc.model = @
      rc.bind = (cb, ctx)=> @bind "change:#{field}", cb, ctx
      rc.unbind = (cb)=> @unbind "change:#{field}", cb
      rc.save = (options)=>
        options = _.extend({
          url: "#{@url()}/#{field}.json"
          type: "PUT"
          processData: false
          error: (resp)=>
            @trigger("error", @, resp)
        }, options)

        if !options.data? 
          options.data = @get field
          if options.dataType == "json" 
            options.data = JSON.stringify( options.data )
        
        $.ajax(options)
        false
        
      rc

    isNew : ->
      @id == null || @id == 0
            
  Collection = Backbone.Collection.extend(ClassHelpers).extend
    update : (models, options) ->
      models  || (models = [])
      options || (options = {})
      
      map = (keys, values) -> 
        _.reduce( _.zip(keys, values), ((memo, v)->memo[v[0]]=v[1]; memo), {})
      new_keys = _.pluck(models, "id")
      new_models_by_id = map(new_keys, models)
      old_keys = _.pluck(@models, "id")
      old_keys_by_id = map(old_keys, @models)
      
      added = _.difference(new_keys, old_keys)
      removed = _.difference(old_keys, new_keys)
      updated = _.difference(old_keys, removed)
      
      o = {silent: options.silent || false}
      for id in updated
        @get(id).set(new_models_by_id[id], o)
      for id in removed
        @remove(old_keys_by_id[id], o)
      for id in added
        @add(new_models_by_id[id], o);
      
      @
      
    # Fetch the default set of models for this collection, resetting the
    # collection when they arrive. If `op: 'add'` is passed, appends the
    # models to the collection instead of resetting. If `op: 'update'` is 
    # passed, updates the collection instead of resetting.
    fetch : (options) ->
      options || (options = {})
      collection = @;
      success = options.success;
      options.success = (resp, status, xhr) ->
        collection[options.op || 'reset'](collection.parse(resp, xhr), options)
        success(collection, resp) if (success)
      options.error = wrapError(options.error, collection, options)
      (this.sync || Backbone.sync).call(this, 'read', this, options);
      
  Controller =  Backbone.View.extend(ClassHelpers)
    
  ExtensionHelpers = 
    singleton: (extensions, options)->
      x = (this.extend(extensions))
      new x(options)
      
  _.extend Model,ExtensionHelpers
  _.extend Collection,ExtensionHelpers
  _.extend Controller,ExtensionHelpers

  TemplateController = Controller.extend(Backbone.Events).extend
    initialize: ->
      (@model = @options.model) if @options.model
      (@attr = @options.attr) if @options.attr
      (@elements = @options.elements) if @options.elements
      (@template = @options.template) if @options.template
      (@on_render = @options.on_render) if @options.on_render
      (@template_data = @options.template_data) if @options.template_data
      (@options.initialize()) if @options.initialize
      @bind "render", @on_render if @on_render
      
    render: ->
      $(@el).attr(@attr) if( @attr )
      if @template
        data = if @template_data
            @template_data(@) 
          else
            {}
        # try
          contents = @template(data)
          $(@el).html contents
        # catch e
        #   console.warn("Render failure:", e, " of data: ", data, " and template:", @template)
      
      if @elements
        _.each @elements, (field, selector) =>
          @[field]=@$(selector)
        
      this.trigger("render", this);
      this
    
  window.CoffeeBar = 
    ClassHelpers:ClassHelpers
    Model:Model
    Collection:Collection
    Controller:Controller
    TemplateController:TemplateController
    
    model: (options,extensions) ->
      if extensions
        x = Model.extend(extensions)
        new x(options)
      else 
        new Model(options)

    template: (options, extensions) ->
      if extensions
        x = TemplateController.extend(extensions)
        new x(options)
      else 
        new TemplateController(options)

    model_backed_template: (options, extensions) ->
      controller = CoffeeBar.template(_.extend({template_data: -> @model.toJSON()},options), extensions)
      controller.model.bind "change", -> controller.render()
      controller
        
    nested_collection: (name, model)->
      lazy(name, ->
        triggered = false
        url = if typeof(@url)=="function" then @url() else @url
        rc = Collection.singleton
          model: model
          url: url+"/"+name
        rc.reset(@toJSON()[name])

        # Update the nested collection if the model's attribute changes .
        @bind "change:"+name, =>
          unless triggered # avoids update loops
            triggered = true
            rc.update(@toJSON()[name])
            triggered = false
        
        # Update the model's attribute if the nested collection changes.
        rc.bind "all", =>
          unless triggered # avoids update loops
            triggered = true
            r = {}
            r[name]=rc.toJSON()
            @set(r)
            triggered = false
        rc
      )

    
  _.templateSettings = 
    evaluate: /\<\%([\s\S\\n\\r]+?)\%\>/g
    interpolate: /\{\{([\s\S\\n\\r]+?)\}\}/g
  
  KB = 1024
  MB = KB * 1024
  GB = MB * 1024
  TB = GB * 1024
  SECONDS = 1000
  MINUTES = 60 * SECONDS
  HOURS = 60 * MINUTES
  DAYS = 24 * HOURS
  YEARS = 365 * DAYS
  
  window.as_memory = (value) ->
    if value < KB
      _.sprintf "%d bytes", value
    else if value < MB
      _.sprintf "%.2f kb", value / KB
    else if value < GB
      _.sprintf "%.2f mb", value / MB
    else if value < TB
      _.sprintf "%.2f gb", value / GB
    else
      _.sprintf "%.2f gb", value / TB
  
  window.duration = (value) ->
    if value < SECONDS
      _.sprintf "%d ms", value
    else if value < MINUTES
      _.sprintf "%d seconds", value / SECONDS
    else if value < HOURS
      _.sprintf "%d minutes", value / MINUTES
    else if value < DAYS
      _.sprintf "%d hours %s", value / HOURS, duration(value % HOURS)
    else if value < YEARS
      _.sprintf "%d days %s", value / DAYS, duration(value % DAYS)
    else
      _.sprintf "%d years %s", value / YEARS, duration(value % YEARS)
  
  window.duration_since = (value) ->
    duration new Date().getTime() - value
  
  window.bind_accordion_actions = (root) ->
    (root or $("body")).find(".accordion").click(->
      $(this).next().toggle "slow"
      $(this).toggleClass "accordion-opened"
      false
    ).each ->
      next = $(this).next()
      if next.is(":visible") && !next.is(".hide")
        $(this).toggleClass "accordion-opened"
  
  window.bind_menu_actions = (root) ->
    $(root).each ->
      unless $(@).data('menu_dropdown')
        $(@).data('menu_dropdown', true)
        $(root).dropdown()
        