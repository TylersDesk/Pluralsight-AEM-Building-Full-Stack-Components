
(function(fullStack){

    var fullStack = fullStack || {};

    fullStack.template = _.template($("#articleView").html());
    
    console.log(fullStack.fakeJson);

    fullStack.init = function(){
        $('.js-insert-articles').append(fullStack.template({articles:fullStack.fakeJson}));
    };

    //Fire it all
    fullStack.init();

})(fullStack);

console.log(jQuery);