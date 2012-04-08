/**
*
*  jQuery Peter Pan Plugin
*	 Written by Adam Kirk
*  Mysterious Trousers, LLC
*  Version 1.0
*
*
*  Permission is hereby granted, free of charge, to any person
*  obtaining a copy of this software and associated documentation
*  files (the "Software"), to deal in the Software without
*  restriction, including without limitation the rights to use,
*  copy, modify, merge, publish, distribute, sublicense, and/or sell
*  copies of the Software, and to permit persons to whom the
*  Software is furnished to do so, subject to the following
*  conditions:
*
*  The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software.
*
*  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
*  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
*  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
*  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
*  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
*  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
*  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
*  OTHER DEALINGS IN THE SOFTWARE.
*
*
**/




(function( $ ){
	
  $.fn.peterpan = function( options ) {  

    var settings = {
		};

    return this.each(function() {
		
		
      // If options exist, lets merge them
      // with our default settings
	    if ( options ) { 
	      $.extend( settings, options );
	    }
			
			
			var $this = $(this);
			var viewWidth;
			var viewHeight;
			var contentWidth;
			var contentHeight;
			var minX;
			var minY;
			var maxX;
			var maxY;
			var $bezel;
			
			var kright = 39;
			var kleft = 37;
			var kdown = 40;
			var kup = 38;
			
			
			var methods = {
				init : function () {
					$table = $this.children().first();
					
					$this.css({ "position" : "relative" });
					$this.append('<div id="peterpan-bezel" style="position: absolute; background-color: rgba(0, 0, 0, 0.65); -webkit-border-radius: 7px; -moz-border-radius: 7px;"></div>');
					$bezel = $("#peterpan-bezel");
					$bezel.append('<div style="position: relative;"><div style="position: absolute; border: 1px solid #AAA;"></div></div>');
					$bezelBorder = $bezel.children().first().children().first();
					$bezelBorder.append('<div style="position: relative;"><div style="position: absolute; border: 1px solid #AAA;"></div></div>');
					$bezelKey = $bezelBorder.children().first().children().first();
					
					this.resize();
					
					$bezel.hide();
					
					this.registerScrollEvents();
					
				},
				getDimensions : function () {
					viewWidth = $this.width();
					viewHeight = $this.height();
					contentWidth = $table.width();
					contentHeight = $table.height();
					minX = -contentWidth + viewWidth;
					minY = -contentHeight + viewHeight;
					maxX = 0;
					maxY = 0;
				},
				resize : function () {
					this.getDimensions();
					bw = 300;
					bh = 250;
					bx = (viewWidth - bw) / 2.0;
					by = (viewHeight - bh) / 2.0;
					$bezel.css({ 	"width" : bw,
												"height" : bh, 
												"left" : bx, 
												"top" : by
											});
											
					bbw = $bezel.width() * 0.9;
					bbh = $bezel.height() * 0.9;
					bbx = $bezel.width() * 0.05;
					bby = $bezel.height() * 0.05;
					$bezelBorder.css({	"width" : bbw, 
															"height" : bbh, 
															"left" : bbx, 
															"top" : bby
														});
														
					bkw = $bezelBorder.width() * ((viewWidth / contentWidth) > 1 ? 1 : (viewWidth / contentWidth));
					bkh = $bezelBorder.height() * ((viewHeight / contentHeight) > 1 ? 1 : (viewHeight / contentHeight));
					bkx = 0;
					bky = 0;
					$bezelKey.css({	"width" : bkw, 
													"height" : bkh, 
													"left" : bkx, 
													"top" : bky
												});														
				},
				registerScrollEvents : function () {
					
					function hookEvent(element, eventName, callback)
					{
					  if(typeof(element) == "string")
					    element = document.getElementById(element);
					  if(element == null)
					    return;
					  if(element.addEventListener)
					  {
					    if(eventName == 'mousewheel')
					      element.addEventListener('DOMMouseScroll', callback, false);  
					    element.addEventListener(eventName, callback, false);
					  }
					  else if(element.attachEvent)
					    element.attachEvent("on" + eventName, callback);
					}
					
					
					function cancelEvent(e)
					{
					  e = e ? e : window.event;
					  if(e.stopPropagation)
					    e.stopPropagation();
					  if(e.preventDefault)
					    e.preventDefault();
					  e.cancelBubble = true;
					  e.cancel = true;
					  e.returnValue = false;
					  return false;
					}
					
					function scrollBasedOnBrowser() {
						if(navigator.userAgent.indexOf("Chrome") != -1) {
							return 1;
						} else {
							return 150.0;
						}
					}
					
					
					hookEvent($this.context, 'mousewheel', function(e) {

						$bezel.stop(true, true);
						newX = currentX = $table.css("margin-left").replace(/[^\.\-0-9]/g,"") * 1;
						newY = currentY = $table.css("margin-top").replace(/[^\.\-0-9]/g,"") * 1;
							
					  e = e ? e : window.event;
					  var wheelDataY = e.detail ? e.detail * -1 : e.wheelDelta / scrollBasedOnBrowser();
						var wheelDataX = 0;
												
						if (e.wheelDeltaX) {
							wheelDataX = e.wheelDeltaX / scrollBasedOnBrowser();
							newX = currentX + wheelDataX;
							if (newX < minX && newX < maxX) newX = minX;
							if (newX > maxX && newX > minX) newX = maxX;
						  $table.css( "margin-left" ,  newX);
						}
						
						if (e.wheelDeltaY) {
							wheelDataY = e.wheelDeltaY / scrollBasedOnBrowser();						
						}
						
						newY = currentY + wheelDataY;
						if (newY < minY && newY < maxY) newY = minY;
						if (newY > maxY && newY > minY) newY = maxY;
						$table.css( "margin-top" ,  newY);
						
						methods.moveBezelTo(newX, newY);
						$bezel.fadeOut(500);
											
					  return cancelEvent(e);
					});
				},
				
				
				animateBezelTo : function (x, y) {
					$bezel.show();
					nX = -$bezelBorder.outerWidth() * (x / contentWidth);
					nY = -$bezelBorder.outerHeight() * (y / contentHeight);
					$bezelKey.animate({ "left" : nX, "top" : nY }, 200);							
				},
				
				
				moveBezelTo : function (x, y) {
					$bezel.show();
					nX = -$bezelBorder.outerWidth() * (x / contentWidth);
					nY = -$bezelBorder.outerHeight() * (y / contentHeight);
					$bezelKey.css({ "left" : nX, "top" : nY });							
				}
			}
			
			
			
			var keyQueue = {
				queue : [],
				currentKey 	: null,
				add : function (key) {
					if (key > 40 || key < 37) return;
					this.queue.push(key);
					if (this.currentKey == null) {
						this.nextKey();
					}
				},
				nextKey : function () {
					if (this.queue.length == 0){
						this.currentKey = null;
						setTimeout(function () {
							if (keyQueue.queue.length == 0) {
								$bezel.fadeOut(500);
							}
						}, 200);
						return;
					}
					this.currentKey = this.queue.shift();
					this.doKey();
				},
				doKey : function () {
					$bezel.stop(true, true);
					newX = currentX = $table.css("margin-left").replace(/[^\.\-0-9]/g,"") * 1;
					newY = currentY = $table.css("margin-top").replace(/[^\.\-0-9]/g,"") * 1;
						
					if (this.currentKey == kright) {	// right (39)
						newX = currentX - viewWidth;
						if (newX < minX) {
							newX = minX;
							if (newX == currentX || viewWidth > contentWidth) {
								if (viewWidth > contentWidth) newX = currentX;
								this.queue = [];								
								$table.animate({ "margin-left" : newX - 50 }, { duration : 80, easing : "easeOutSine", complete : function () {
									$table.animate({ "margin-left" : newX }, { duration : 80, easing : "easeInSine", complete : keyQueue.animationComplete} );
								}});
							} else {
								$table.animate({ "margin-left" : newX }, { duration : 100, easing : "easeOutBack", complete : keyQueue.animationComplete} );								
							}
						} else {
							newX = this.getNextRightX();
							$table.animate({ "margin-left" : newX }, { duration : 100, complete : keyQueue.animationComplete} );
						}
					}
					if (this.currentKey == kleft) {	// left (37)
						newX = currentX + viewWidth;
						if (newX > maxX) {
							newX = maxX;
							if (newX == currentX) {
								this.queue = [];
								$table.animate({ "margin-left" : newX + 50 }, { duration : 80, easing : "easeOutSine", complete : function () {
									$table.animate({ "margin-left" : newX }, { duration : 80, easing : "easeInSine", complete : keyQueue.animationComplete} );
								}});
							} else {
								$table.animate({ "margin-left" : newX }, { duration : 100, easing : "easeOutBack", complete : keyQueue.animationComplete} );								
							}
						} else {
							newX = this.getNextLeftX();
							$table.animate({ "margin-left" : newX }, { duration : 100, complete : keyQueue.animationComplete} );
						}
					}
					else if (this.currentKey == kup) {	// up (38)
						newY = currentY + viewHeight;
						if (newY > maxY) {
							newY = maxY;
							if (newY == currentY) {
								this.queue = [];
								$table.animate({ "margin-top" : newY + 50 }, { duration : 80, easing : "easeOutSine", complete : function () {
									$table.animate({ "margin-top" : newY }, { duration : 80, easing : "easeInSine", complete : keyQueue.animationComplete} );
								}});
							} else {
								$table.animate({ "margin-top" : newY }, { duration : 100, easing : "easeOutBack", complete : keyQueue.animationComplete} );
							}
						} else {
							newY = this.getNextUpY();
							$table.animate({ "margin-top" : newY }, { duration : 100, complete : keyQueue.animationComplete} );
						}
					}
					else if (this.currentKey == kdown) {	// down (40)
						newY = currentY - viewHeight;
						if (newY < minY) {
							newY = minY;
							if (newY == currentY || viewHeight > contentHeight) {
								if (viewHeight > contentHeight) newY = currentY;
								this.queue = [];
								$table.animate({ "margin-top" : newY - 50 }, { duration : 80, easing : "easeOutSine", complete : function () {
									$table.animate({ "margin-top" : newY }, { duration : 80, easing : "easeInSine", complete : keyQueue.animationComplete} );
								}});
							} else {
								$table.animate({ "margin-top" : newY }, { duration : 100, easing : "easeOutBack", complete : keyQueue.animationComplete} );								
							}
						} else {
							newY = this.getNextDownY();
							$table.animate({ "margin-top" : newY }, { duration : 100, complete : keyQueue.animationComplete} );
						}
					}
					methods.animateBezelTo(newX, newY);
				},
				getNextRightX : function () {
					currentX = Math.abs($table.css("margin-left").replace(/[^\.\-0-9]/g,"") * 1);
					mX = currentX + viewWidth;
					currentCellsWidth = 0;
					previousCellsWidth = 0;
					$($table).find("tr:first-child td").each(function () {
						previousCellsWidth = currentCellsWidth;
						currentCellsWidth += $(this).outerWidth();
						if (currentCellsWidth > mX) {
							return false;
						}
					});
					return -previousCellsWidth;						
				},
				getNextLeftX : function () {
					currentX = Math.abs($table.css("margin-left").replace(/[^\.\-0-9]/g,"") * 1);
					mX = currentX - viewWidth;
					currentCellsWidth = 0;
					$($table).find("tr:first-child td").each(function () {
						currentCellsWidth += $(this).outerWidth();
						if (currentCellsWidth > mX) {
							return false;
						}
					});
					return -currentCellsWidth;
				},
				getNextDownY : function () {
					currentY = Math.abs($table.css("margin-top").replace(/[^\.\-0-9]/g,"") * 1);
					mY = currentY + viewHeight;
					currentCellsHeight = 0;
					previousCellsHeight = 0;
					$($table).find("tr td:first-child").each(function () {
						previousCellsHeight = currentCellsHeight;
						currentCellsHeight += $(this).outerHeight();
						if (currentCellsHeight > mY) {
							return false;
						}
					});
					return -previousCellsHeight;		
				},
				getNextUpY : function () {
					currentY = Math.abs($table.css("margin-top").replace(/[^\.\-0-9]/g,"") * 1);
					mY = currentY - viewHeight;
					currentCellsHeight = 0;
					$($table).find("tr td:first-child").each(function () {
						currentCellsHeight += $(this).outerHeight();
						if (currentCellsHeight > mY) {
							return false;
						}
					});
					return -currentCellsHeight;
				},
				animationComplete : function () {
					keyQueue.nextKey.apply(keyQueue)
				},
			};
			
			
			
			
			
			
			
			methods.init();
			
			//$this.bind('resize.peterpan', function (event) {
			$(window).resize(function () {
				methods.resize();
			});
			
			$(window).bind('keyup.peterpan', function (event) {
				keyQueue.add(event.which);
				return false;
			});
			
    });
  };

})( jQuery );
