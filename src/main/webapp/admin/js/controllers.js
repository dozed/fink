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
  $scope.post = new Post({catId: 0, date: "", title: "", author: "", text: "", tags: []});

  $scope.save = function() {
    $scope.post.id = 0;
    if ($scope.selectedCategory != null) {
      $scope.post.catId = parseInt($scope.selectedCategory);
      $scope.post.category = _.find($scope.categories, function(c) { return c.id == $scope.post.catId });
    } else {
      $scope.post.catId = 0;
    }
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

  $scope.gallery = new Gallery({date: 0, title: "", author: "", shortlink: "", text: "", tags: []});

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

function EditGalleryController($scope, $location, $routeParams, Gallery, Tag, Image) {
  var self = this;
  self.original = null;

  $scope.fink_base = fink_base
  $scope.tags = Tag.query();
  $scope.galleries = Gallery.query();

  Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
    self.original = gallery;
    $scope.gallery = new Gallery(gallery);

    $('#file_upload').uploadify({
      'swf' : 'lib/uploadify-3.1.1/uploadify.swf',
      'folder'    : '/uploads',
      'auto'      : true,
      'multi'     : true,
      'method'    : 'POST',
      'debug'     : false,
      'uploader'  : fink_base+'/admin/api/images',
      'checkExisting' : false,
      'fileObjName'   : 'file',
      'fileTypeExts'  : '*.jpg;*.jpeg;*.gif;*.png',
      'fileTypeDesc'  : 'Image Files (.JPG, .GIF, .PNG)',
      'buttonClass'   : 'btn',
      'height'        : 18,
      'width'         : 42,
      'buttonText'    : 'Upload',
      'onQueueComplete' : function() {
        Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
          $scope.gallery = new Gallery(gallery);
        })
      },
      'onUploadSuccess' : function(file, data, response) {
        var imageId = data;
        $.post(fink_base+"/admin/api/galleries/"+gallery.id+"/images/"+imageId)
      } 
    });

    $("#albums-images").sortable({
      update: function(event, ui) {
        var order = _.map($("#albums-images").sortable('toArray'), function(id) { return id.split('-')[1]; }).join(',');
        $.post(fink_base+"/admin/api/galleries/"+$scope.gallery.id+"/images", {order: order});
      }
    });

    if (gallery.coverId != 0) {

    } else {
      $scope.cover = fink_base+"/admin/images/noimage.png"
    }
  })

  $scope.setCover = function() {
    
  }

  $scope.enterImage = function(image) {
    $("#image-"+image.id).addClass('selected');
  }

  $scope.leaveImage = function(image) {
    $("#image-"+image.id).removeClass('selected');
  }

  $scope.removeImage = function(image) {
    $.delete(fink_base+"/admin/api/galleries/"+$scope.gallery.id+"/images/"+image.id, function(data, status) {
      Gallery.get({galleryId: $routeParams.galleryId}, function(gallery) {
        $scope.gallery = new Gallery(gallery);
      })
    })
  }

  $scope.editImage = function(image) {
    $scope.image = image;
    $scope.originalImage = new Image(image);
    $scope.imageEditing = true;
  }

  $scope.finishEditing = function() {
    if (!angular.equals($scope.originalImage, $scope.image)) {
      Image.update($scope.image, function(data, status) {
        $scope.image = null;
        $scope.imageEditing = false;
      })
    } else {
      $scope.imageEditing = false;
    }
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
  $scope.page = new Page({date: 0, title: "", author: "", shortlink: "", text: "", tags: []});

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
