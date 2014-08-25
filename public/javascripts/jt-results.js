var resultsSelectedSong;
var resultsHandlerAdded = false;

function resultsAddHandlers(evt) {
	if ( resultsHandlerAdded ) {
		return;
	}
	resultsHandlerAdded = true;
	$("#play-button").unbind('click').live('click', function(PBEvt) {
		resultsPlay();
		return false;
	});

	$("#playlist-button").unbind('click').live('click', function(PLBEvt) {
		resultsEnqueue();
		return false;
	});

	$(".search-result").unbind('click').live('click',resultsSelected);

};

function resultsSelected() {
		var id = $(this).attr('id');
		for (f in songs) {
			if (songs[f].id == id) {
				resultsSelectedSong = songs[f];
				break;
			}
		}
		return true;
}

function resultsPlay() {
	if ( resultsSelectedSong == undefined ) {
		return;
	}
	console.debug("playing");
	$.mobile.showPageLoadingMsg();
	$.post('/play',{song:resultsSelectedSong},function(r) {
		$.mobile.changePage('/playback',{transition:'fade'});
	});				
}

function resultsEnqueue() {
	if ( resultsSelectedSong == undefined ) {
		return;
	}
	$.post('/playlist/add',{song:resultsSelectedSong},function(r) {
	});
}
