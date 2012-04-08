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
  "views/jade"
], (jade)->
  
  #
  # new EditablePropertyController
  #   el: @$("name")
  #   property: @model.property("name")
  #
  table = CoffeeBar.TemplateController.extend

    template: jade["editable_property_controller.jade"]
    template_data: -> 
      state: @state.toJSON()
      property: @property()
    
    initialize: ->
      @state = new CoffeeBar.Model
        editing:false
      @property = @options.property
      @on_save = @options.on_save if @options.on_save
      CoffeeBar.TemplateController.prototype.initialize.call(this);
      @property.bind @render, @
      @state.bind "all", @render, @

    remove: -> 
      @property.unbind @render
      CoffeeBar.TemplateController.prototype.remove.call(this);
      
    save: ->
      update = @$("input").val()
      @property(update)
      @on_save()
      @state.set
        editing:false
      
    on_render: ->
      if @state.get("editing")
        @$("form").submit => @save(); false
        @$("a.save").click => @save(); false
        @$("a.cancel").click =>
          @state.set
            editing:false
          false
          
      else
        @$("a.edit").click =>
          @state.set
            editing:true

    on_save: ->
      @property.model.save()
      