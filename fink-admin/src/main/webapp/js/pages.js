/**
 * 
 */

$(document).ready(function() {
	create_page_entry(pages, $(".pages"), 0);
});

function create_page_entry(page, container, level) {

	var entry = $('<div class="entry">'
			+	'<div class="info"><span class="control"></span><span class="title"><a href="#"></a></span><div class="clear"></div></div>'
			+ '<div class="entries"></div>'
			+ '</div>');
	container.append(entry);

	var entries = entry.find("div.entries");
	var title = entry.find("span.title a").html(page.title);
	
	if (level>0) {
		title.attr("href", ADMIN + "pages/edit/" + page.uuid);
	}
	
	if (page.subPages.length > 0) {
		var ctrl = entry.find("span.control");

		if (level>0) {
			entries.css("display", "none");
			ctrl.addClass("folder-closed");
		} else {
			ctrl.addClass("folder-open");
		}
		
		ctrl.click(function(el) {
			ctrl.toggleClass("folder-open");
			ctrl.toggleClass("folder-closed");
			entries.toggle(250);
		});
	}
	
	$.each(page.subPages, function(idx, p) {
		create_page_entry(p, entries, level+1);
	});
}