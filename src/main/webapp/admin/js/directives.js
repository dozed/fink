'use strict';

var directive={};

directive.tabbable = function() {
  return {
    restrict: 'C',
    compile: function(element) {
      var navTabs = angular.element('<ul class="nav nav-tabs"></ul>'),
          tabContent = angular.element('<div class="tab-content"></div>');

      tabContent.append(element.contents());
      element.append(navTabs).append(tabContent);
    },
    controller: ['$scope', '$element', function($scope, $element) {
      var navTabs = $element.contents().eq(0),
        ngModel = $element.controller('ngModel') || {},
        tabs = [],
        selectedTab;

      ngModel.$render = function() {
        var $viewValue = this.$viewValue;

        if (selectedTab ? (selectedTab.value != $viewValue) : $viewValue) {
          if(selectedTab) {
            selectedTab.paneElement.removeClass('active');
            selectedTab.tabElement.removeClass('active');
            selectedTab = null;
          }
          if($viewValue) {
            for(var i = 0, ii = tabs.length; i < ii; i++) {
              if ($viewValue == tabs[i].value) {
                selectedTab = tabs[i];
                break;
              }
            }
            if (selectedTab) {
              selectedTab.paneElement.addClass('active');
              selectedTab.tabElement.addClass('active');
            }
          }

        }
      };

      this.addPane = function(element, attr) {
        var li = angular.element('<li><a href></a></li>'),
            a = li.find('a'),
            tab = {
              paneElement: element,
              paneAttrs: attr,
              tabElement: li
            };

        tabs.push(tab);

        attr.$observe('value', update)();
        attr.$observe('title', function(){ update(); a.text(tab.title); })();

        function update() {
          tab.title = attr.title;
          tab.value = attr.value || attr.title;
          if (!ngModel.$setViewValue && (!ngModel.$viewValue || tab == selectedTab)) {
            // we are not part of angular
            ngModel.$viewValue = tab.value;
          }
          ngModel.$render();
        }

        var idx=$(element).index();
        var prev=navTabs.children().eq(idx);
        prev.length?prev.before(li):navTabs.append(li);

        li.bind('click', function(event) {
          event.preventDefault();
          event.stopPropagation();
          if (ngModel.$setViewValue) {
            $scope.$apply(function() {
              ngModel.$setViewValue(tab.value);
              ngModel.$render();
            });
          } else {
            // we are not part of angular
            ngModel.$viewValue = tab.value;
            ngModel.$render();
          }
        });

        return function() {
          tab.tabElement.remove();
          for(var i = 0, ii = tabs.length; i < ii; i++ ) {
            if (tab == tabs[i]) {
              tabs.splice(i, 1);
            }
          }
        };
      }
    }]
  };
};


directive.tabPane = function() {
  return {
    require: '^tabbable',
    restrict: 'C',
    link: function(scope, element, attrs, tabsCtrl) {
      element.bind('$remove', tabsCtrl.addPane(element, attrs));
    }
  };
};

directive.popover = function() {
  return {
    restrict: 'A',
    compile: function(element, attrs) {
      element.popover();
    }
  }
}

directive.bootstrapModal = function($timeout) {
  var link = function(scope, elm, attrs) {
    var escapeEvent;
    var openModal;
    var closeModal;

    //Escape event has to be declared so that when modal closes,
    //we only unbind modal escape and not everything
    escapeEvent = function(e) {
      if (e.which == 27) closeModal();
    };

    openModal = function(event, hasBackdrop, hasEscapeExit) {
      var modal = jQuery('#'+attrs.modalId);

      //Make click on backdrop close modal
      if (hasBackdrop === true) {
        //If no backdrop el, have to add it
        if (!document.getElementById('modal-backdrop')) {
            jQuery('body').append(
              '<div id="modal-backdrop" class="modal-backdrop"></div>'
            );
        }
        jQuery('#modal-backdrop').
          css({ display: 'block' }).
          bind('click', closeModal);
      }

      //Make escape close modal
      if (hasEscapeExit === true)
        jQuery('body').bind('keyup', escapeEvent);
      
      //Add modal-open class to body
      jQuery('body').addClass('modal-open');

      //Find all the children with class close,
      //and make them trigger close the modal on click
      jQuery('.close', modal).bind('click', closeModal);

      modal.css({ display: 'block' });
    };
    
    closeModal = function(event) {
      jQuery('#modal-backdrop').
        unbind('click', closeModal).
        css({ display: 'none' });
      jQuery('body').
        unbind('keyup', escapeEvent).
        removeClass('modal-open');
      jQuery('#'+attrs.modalId).css({ display: 'none' });
      event.stopPropagation();
      event.preventDefault();
    };

    //Bind modalOpen and modalClose events, so outsiders can trigger it
    //We have to wait until the template has been fully put in to do this,
    //so we will wait 100ms
    $timeout(function() {
      jQuery('#'+attrs.modalId).
        bind('modalOpen', openModal).
        bind('modalClose', closeModal);
    }, 100);
  };

  return {
    link: link,
    restrict: 'E',
    scope: {
      modalId: '@'
    },
    template: '<div id="{{modalId}}" class="modal hide"><div ng-transclude></div></div>',
    transclude: true
  };
}

directive.bootstrapModalOpen = function() {
  return {
    restrict: 'A',
    link: function(scope, elm, attrs) {
      var hasBackdrop = attrs.backdrop === undefined ? true : attrs.backdrop;
      var hasEscapeExit = attrs.escapeExit === undefined ? true : attrs.escapeExit;

      //Allow user to specify whether he wants it to open modal on click or what
      //Defaults to click
      var eventType = attrs.modalEvent === undefined ? 'click' : eventType;
      
      jQuery(elm).bind(eventType, function() {
        jQuery('#'+attrs.bootstrapModalOpen).trigger(
          'modalOpen', [hasBackdrop, hasEscapeExit]
        );
      });
    }
  };
}

directive.textEditor = function() {

  function loadAceEditor(element, mode) {
    var editor = ace.edit(element);
    editor.session.setMode("ace/mode/" + mode);
    editor.renderer.setShowPrintMargin(false);

    return editor;
  }

  function valid(editor) {
    return (Object.keys(editor.getSession().getAnnotations()).length == 0);
  }

  return {
    restrict: 'E',
    require: '?ngModel',

    link: function(scope, element, attrs, ngModel) {
      var pre = $("<pre></pre>")[0];
      $(pre).width(attrs.width);
      $(pre).height(attrs.height);
      element.html(pre);

      var mode = attrs.mode;
      var editor = loadAceEditor(pre, mode);

      scope.ace = editor;

      if (!ngModel) return;

      ngModel.$render = function() {
        var value = ngModel.$viewValue || '';
        editor.getSession().setValue(value);
      };

      editor.getSession().on('changeAnnotation', function() {
        if (valid(editor)) {
          scope.$apply(read);
        }
      });

      editor.getSession().on('change', function(e) {
        ngModel.$setViewValue(editor.getValue());
      });

      read();

      function read() {
        ngModel.$setViewValue(editor.getValue());
      }
    }
  }
}

directive.tagManager = function() {
  return {
    restrict:'E',
    scope:{
      tags: '=ngModel'
    },
    replace:true,
    template:'<div class="tagManager">' +
      '<span class="label label-inverse" style="text-align:inline; margin-right:3px;" ng-repeat="tag in tags">{{tag}} <a ng-click="removeTag(tag)" alt="Remove tag">&times;</a></span>' +
      '<input type="text" ng-model="tagField" placeholder="Enter , separated tags" style="vertical-align: baseline;"/>' +
      '</div>',

    link: function(scope, element, attrs) {
      scope.tagField = '';

      // Watching update on tagField to handle new comma input
      scope.$watch('tagField', function(value){
        if (value!= null) {
          var values = value.split(/[,;\s]/);
          if (values.length > 1) {
            while (values.length>1) {
              scope.addTag(values.shift());
            }
            scope.tagField=values.pop();
          }
        }
      });

      // Remove a tag
      scope.removeTag = function(tag) {
        scope.tags = scope.tags.filter(function(currentTag){
          return currentTag.toLowerCase() != tag.toLowerCase();
        });
      };

      // Add a tag
      scope.addTag = function(tag){
        // Remove previous occurence if exists (avoid duplicated tag)
        scope.removeTag(tag);
        // Add tag to tagList
        scope.tags.push(tag);
      }

      // switch tag from tagField to TagList
      scope.switchToTagList = function(){
        scope.$apply(function() {
          if (scope.tagField.trim().length > 0) {
            scope.addTag(scope.tagField.trim());
            scope.tagField = '';
          }
        });
      }

      // Registering event on backspace, enter or lost focus
      $(element).first('input')
        .keydown(function(e){
          // On backspace load the previous tag into tagField input
          if(e.keyCode == 8) {
            scope.$apply(function() {
              if (scope.tagField=='') {
                scope.tagField = scope.tags.pop();
              }
            });
          // On Enter switch tag to taglist
          } else if(e.keyCode == 13){
            scope.switchToTagList();
          }
        })
        // On lost focus -> add current tagfield content into tags array
        .focusout(function(e){
          scope.switchToTagList();
        });
    }
  }
}

angular.module('fink.directives', ['ng', 'fink.resources']).directive(directive)
