this.imagePreview = function () {
    xOffset = 10;
    yOffset = 30;

    $(".image-preview").hover(
        function (e) {
            this.t = this.title;
            this.title = "";
            var c = (this.t != "") ? "<br/>" + this.t : "";
            $("body").append("<div id='preview'><img src='" + this.dataset.img + "' alt='Image preview' />" + c + "</div>");
            $("#preview")
                .css("top", (e.pageY - xOffset) + "px")
                .css("left", (e.pageX + yOffset) + "px")
                .fadeIn("fast");
        },
        function () {
            this.title = this.t;
            $("#preview").remove();
        }
    );

    $(".image-preview").mousemove(function (e) {
        $("#preview")
            .css("top", (e.pageY - xOffset) + "px")
            .css("left", (e.pageX + yOffset) + "px");
    });
};


// starting the script on page load
$(document).ready(function () {
    imagePreview();
});