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