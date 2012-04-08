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
  "controllers/collection_controller"
], (CollectionController)->
  
  #
  # new table 
  #   template: jade["some-table.jade"]
  #   row_template: jade["some-tr.jade"]
  #   collection: A Collection
  #
  table = CoffeeBar.TemplateController.extend
    tagName: "table"
    initialize: ->
      CoffeeBar.TemplateController.prototype.initialize.call(this);
      @row_template = @options.row_template if @options.row_template
      @row_template_data = @options.row_template_data if @options.row_template_data
      @collection_controller = new CollectionController
        tagName: "tbody"
        collection: @options.collection
        child_control: (model)=> @child_control(model)
      
      @row_controls = @collection_controller.child_controls
      
    on_render: ->
      tbody = @collection_controller.render().el
      $(@el).append(tbody)
    
    row_template_data: (model)-> model.toJSON()
    
    child_control: (model)->
      controller = new CoffeeBar.TemplateController
        model: model
        tagName: "tr"
        template: @options.row_template
        template_data: => @row_template_data(model)
      if @options.on_row_render
        controller.bind "render", @options.on_row_render 
      model.bind "change", -> controller.render()
      controller
