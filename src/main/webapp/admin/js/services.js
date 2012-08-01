'use strict';

var methods = {
  'get':    {method:'GET'},
  'save':   {method:'POST'},
  'update': {method:'PUT'},
  'query':  {method:'GET', isArray:true},
  'remove': {method:'DELETE'},
  'delete': {method:'DELETE'}};

angular.module('fink.resources', ['ngResource']).factory('Tag', function($resource){
  return $resource(fink_base+'/admin/api/tags/:tagId', {tagId:'@id'}, methods);
}).factory('Post', function($resource){
  return $resource(fink_base+'/admin/api/posts/:postId', {postId:'@id'}, methods);
}).factory('Category', function($resource){
  return $resource(fink_base+'/admin/api/categories/:categoryId', {categoryId:'@id'}, methods);
}).factory('Gallery', function($resource){
  return $resource(fink_base+'/admin/api/galleries/:galleryId', {galleryId:'@id'}, methods);
}).factory('Page', function($resource){
  return $resource(fink_base+'/admin/api/pages/:pageId', {pageId:'@id'}, methods);
}).factory('Image', function($resource){
  return $resource(fink_base+'/admin/api/images/:imageId', {imageId:'@id'}, methods);
}).value('version', '0.1');
