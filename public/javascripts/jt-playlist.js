var playlistInitDone = false;

function playlistInit() {
	if ( playlistInitDone ) {
		return;
	}
	playlistInitDone = true;
	$('#clear-playlist').unbind('click').live('click',function(evt) {
		playlistClear();
	});
	$('.playlist-item').unbind('click').live('click',function(evt) {
		playlistPlay($(evt.target).attr('id'));
	});
}

function playlistClear() {
	$.mobile.showPageLoadingMsg();
	$.get('/playlist/clear',null,function(r) {
		document.location.href = document.location.href;				
	});
	return false;
}

function playlistPlay(playlistItemId) {
	var selectedPLI;
	for (i in playlistItems) {
		if ( playlistItems[i].playlistId != playlistItemId ) {
			continue;
		}
		selectedPLI = playlistItems[i];
		break;
	}
	if ( selectedPLI == undefined ) {
		return;
	}
	
	$.post('/playlist/play',{playlistItem:selectedPLI},function(r) {
		
	});
}
