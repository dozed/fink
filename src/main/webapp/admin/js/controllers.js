'use strict';

function PostController($scope, Tag, Post) {
  $scope.tags = Tag.query();
  $scope.posts = Post.query();

  $scope.delete = function(id) {
    Post.delete({postId: id}, function() {
      $scope.posts = Post.query();
    })
  }
}

function CreatePostController($scope, $location, Tag, Post, Category) {
  $scope.tags = Tag.query();
  $scope.posts = Post.query();
  $scope.categories = Category.query();
  $scope.post = new Post({tags: []});

  $scope.save = function() {
    $scope.post.id = 0;
    $scope.post.catId = parseInt($scope.selectedCategory);
    $scope.post.category = _.find($scope.categories, function(c) { return c.id == $scope.post.catId });
    $scope.post.date = new Date().getTime();

    Post.save($scope.post, function(post) {
      $location.path('/posts');
    });
  }

  $scope.cancel = function() {
    $location.path('/posts');
  }
}

function EditPostController($scope, $location, $routeParams, Post, Tag, Category) {
  var self = this;
  self.original = null;

  $scope.tags = Tag.query();
  $scope.categories = Category.query();

  Post.get({postId: $routeParams.postId}, function(post) {
    self.original = post;
    $scope.post = new Post(post);
    $scope.selectedCategory = $scope.post.category.id;
  })

  $scope.isClean = function() {
    return angular.equals(self.original, $scope.post) && (self.original != null && self.original.catId == $scope.selectedCategory.id);
  }

  $scope.cancel = function() {
    $location.path('/posts');
  }

  $scope.save = function() {
    $scope.post.catId = parseInt($scope.selectedCategory);
    $scope.post.category = _.find($scope.categories, function(c) { return c.id == $scope.post.catId });

    Post.update($scope.post, function(post) {
      $location.path('/posts');
    });
  }
}

function GalleryController($scope, Gallery, Tag) {
  $scope.tags = Tag.query();
  $scope.galleries = Gallery.query();

  $scope.delete = function(id) {
    Gallery.delete({galleryId: id}, function() {
      $scope.galleries = Gallery.query();
    })
  }
}

function CreateGalleryController($scope, $location, Gallery, Tag) {
  $scope.tags = Tag.query();
  $scope.galleries = Gallery.query();

  // TODO dummy object
  $scope.gallery = new Gallery({tags: []});

  $scope.save = function() {
    $scope.gallery.id = 0;
    $scope.gallery.coverId = 0;
    $scope.gallery.date = new Date().getTime();

    Gallery.save($scope.gallery, function(gallery) {
      $location.path('/galleries');
    });
  }

  $scope.cancel = function() {
    $location.path('/galleries');
  }
}

function EditGalleryController($scope, $location, $routeParams, Gallery, Tag) {
  var self = this;
  self.original = null;

  $scope.tags = Tag.query();
  $scope.galleries = Gallery.query();

  Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
    self.original = gallery;
    $scope.gallery = new Gallery(gallery);
  })

  $scope.onRender = function(a) {
    $scope.$watch("gallery.id", function(galleryId) {
      if (galleryId != undefined) {
        $('#file_upload').uploadify({
          'swf' : 'lib/uploadify-3.1.1/uploadify.swf',
          'folder'    : '/uploads',
          'auto'      : true,
          'multi'     : true,
          'method'    : 'POST',
          'debug'     : false,
          'uploader'  : 'http://localhost:8080/admin/api/images',
          'checkExisting' : false,
          'fileObjName'   : 'file',
          'fileTypeExts'  : '*.jpg;*.jpeg;*.gif;*.png',
          'fileTypeDesc'  : 'Image Files (.JPG, .GIF, .PNG)',
          'onQueueComplete' : function() {
            Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
              $scope.gallery = new Gallery(gallery);
            })
          },
          'onUploadSuccess' : function(file, data, response) {
            var imageId = data;
            $.post("/admin/api/galleries/"+galleryId+"/images/"+imageId, function(data, status) {
              console.log(data + " - " + status);
            })
          } 
        });

        $("#albums-images").sortable({
          update: function(event, ui) {
            // var order = $("#images-list").sortable('toArray').join(',');
            // $.post("#{adminBase("/collections/edit/"+collection.getUuid+"/sort")}", {
            //   order: order
            // }, function() {
            //   console.log("done");
            // });
            var order = _.map($("#albums-images").sortable('toArray'), function(id) { return id.split('-')[1]; }).join(',');
            $.post("/admin/api/galleries/"+$scope.gallery.id+"/images", {order: order}, function() {
              console.log("done");
            });
          }
        });

      }
    });
  }

  $scope.$on('$viewContentLoaded', $scope.onRender);

  $scope.enterImage = function(image) {
    $("#image-"+image.id).addClass('selected');
  }

  $scope.leaveImage = function(image) {
    $("#image-"+image.id).removeClass('selected');
  }

  $scope.removeImage = function(image) {
    $.delete("/admin/api/galleries/"+$scope.gallery.id+"/images/"+image.id, function(data, status) {
      Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
        $scope.gallery = new Gallery(gallery);
      })
    })
  }

  $scope.isClean = function() {
    return angular.equals(self.original, $scope.gallery);
  }

  $scope.cancel = function() {
    $location.path('/galleries');
  }

  $scope.save = function() {
    Gallery.update($scope.gallery, function(gallery) {
      $location.path('/galleries');
    });
  }
}

function PageController($scope, Page) {
  $scope.pages = Page.query();

  $scope.delete = function(id) {
    Page.delete({pageId: id}, function() {
      $scope.pages = Page.query();
    })
  }
}

function CreatePageController($scope, $location, Page) {
  $scope.pages = Page.query();
  $scope.page = new Page({tags: []});

  $scope.save = function() {
    $scope.page.id = 0;
    $scope.page.date = new Date().getTime();

    Page.save($scope.page, function(page) {
      $location.path('/pages');
    });
  }

  $scope.cancel = function() {
    $location.path('/pages');
  }
}

function EditPageController($scope, $location, $routeParams, Page) {
  var self = this;
  self.original = null;

  Page.get({pageId: $routeParams.pageId}, function(page) {
    self.original = page;
    $scope.page = new Page(page);
  })

  $scope.isClean = function() {
    return angular.equals(self.original, $scope.page);
  }

  $scope.cancel = function() {
    $location.path('/pages');
  }

  $scope.save = function() {
    Page.update($scope.page, function(page) {
      $location.path('/pages');
    });
  }
}
