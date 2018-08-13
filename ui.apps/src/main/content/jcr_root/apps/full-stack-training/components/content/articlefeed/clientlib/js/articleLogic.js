
(function(fullStack){

    var fullStack = fullStack || {};

    fullStack.template = _.template($("#articleView").html());
    
    console.log(fullStack.fakeJson);

    fullStack.init = function(){
        // Changes this to use the endpoint data..
        var articleFeeds = $(".article-feed").data('article-feed-json')

        $.ajax({
          url: articleFeeds
        }).done(function(jsonResp) {
            $('.js-insert-articles').append(fullStack.template({articles:jsonResp}));
        });
    };

    //Fire it all
    fullStack.init();

})(fullStack);
