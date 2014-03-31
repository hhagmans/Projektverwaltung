function del(desturl){
$.ajax({
    url: desturl,
    type: 'DELETE',
});
}

function put(desturl){
	$.ajax({
	    url: desturl,
	    type: 'PUT',
	});
}