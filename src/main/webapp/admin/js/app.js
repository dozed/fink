'use strict';

angular.module('myApp', ['fink.filters', 'fink.directives', 'fink.resources']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/posts', {templateUrl: 'partials/posts.html', controller: PostController});
    $routeProvider.when('/posts/create', {templateUrl: 'partials/posts-details.html', controller: CreatePostController});
    $routeProvider.when('/posts/edit/:postId', {templateUrl: 'partials/posts-details.html', controller: EditPostController});
    $routeProvider.when('/galleries', {templateUrl: 'partials/galleries.html', controller: GalleryController});
    $routeProvider.when('/galleries/create', {templateUrl: 'partials/galleries-details.html', controller: CreateGalleryController});
    $routeProvider.when('/galleries/edit/:galleryId', {templateUrl: 'partials/galleries-details.html', controller: EditGalleryController});
    $routeProvider.otherwise({redirectTo: '/posts'});
  }]);


function _ajax_request(url, data, callback, type, method) {
    if (jQuery.isFunction(data)) {
        callback = data;
        data = {};
    }
    return jQuery.ajax({
        type: method,
        url: url,
        data: data,
        success: callback,
        dataType: type
        });
}

jQuery.extend({
    put: function(url, data, callback, type) {
        return _ajax_request(url, data, callback, type, 'PUT');
    },
    delete: function(url, data, callback, type) {
        return _ajax_request(url, data, callback, type, 'DELETE');
    }
});