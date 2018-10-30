
(function(fullStack){

    var fullStack = fullStack || {};

    fullStack.template = _.template($("#articleView").html());
    
    console.log(fullStack.fakeJson);

    fullStack.init = function(){

        var articleFeed = $(".article-feed").data('article-feed-json');

        $.ajax({
            url:articleFeed
        }).done(function(jsonResponse){
            $('.js-insert-articles').append(fullStack.template({articles:jsonResponse}));
        })

        
    };

    //Fire it all
    fullStack.init();

})(fullStack);

console.log(jQuery);