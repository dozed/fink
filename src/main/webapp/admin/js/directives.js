'use strict';

angular.module('fink.directives', ['ng', 'fink.resources'])
  .directive('tagList', function($parse) {
    return {
      restrict: 'E',
      link: function(scope, element, attrs) {
        var el = $('<input type="text" value="">')[0];

        scope.$watch(attrs.tags, function() {
          var tags = scope.$eval(attrs.tags);
          if ($.isArray(tags)) {
            _.map(tags, function(t) { $(el).tagit('createTag', t.name); });
          }
        })

        element.html(el);

        $(el).tagit({
          onTagAdded: function(event, tag) {
            var name = $(tag).find("span.tagit-label").html();
            var tags = scope.$eval(attrs.tags);
            if (!_.any(tags, function(t) { return t.name == name; })) {
              scope.$apply(function() {
                tags.push({ id: 0, name: name, jsonClass: "Tag" })
              });
            }
          },
          
          onTagRemoved: function(event, tag) {
            var name = $(tag).find("span.tagit-label").html();
            scope.$apply(function() {
              var tags2 = _.filter(scope.$eval(attrs.tags), function(t) { return t.name != name; })
              $parse(attrs.tags).assign(scope, tags2)
            });
          }
        });
      }
    }
  })


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

angular.module('bootstrap', []).directive(directive)


angular.module('angularBootstrap.modal', []).
directive('bootstrapModal', function($timeout) {
    var link = function(scope, elm, attrs) {
        var escapeEvent;
        var openModal;
        var closeModal;

        //Escape event has to be declared so that when modal closes,
        //we only unbind modal escape and not everything
        escapeEvent = function(e) {
            if (e.which == 27)
                closeModal();
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
}).
directive('bootstrapModalOpen', function() {
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
});