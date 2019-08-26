$(function () {
    $(".left-menu-btn").click(function () {
        $("#group-user .init-group-hiden").hide();// 隐藏群组的右侧信息
        $(this).siblings().each(function () {
            var othcla = $(this).data("box");
            $("." + othcla).css('display', 'none');
            $(this).css("color", "#000000");
        });
        $(this).css("color", "red");
        var cla = $(this).data("box");
        $("." + cla).css('display', 'block');
    });
    uploadimg();// 初始化上传按钮的点击事件
});

function uploadimg() {
    $("#uploadimgBtn").click(function () {
        var formdata = new FormData($("#uploadimgFrom")[0]);
        $.ajax({
            type: "POST",
            url: "/upload.do?postmark=" + parseInt(Math.random() * (100 - 0 + 1) + 0),
            cache: false,
            data: formdata,
            async: false,
            contentType: false,
            processData: false,
            success: function (data) {
                var htm = "";
                for (var i = 0; i < data.length; i++) {
                    var img = new Image();
                    img.src = data[i];
                    var height = 75;
                    var width = (height / img.height) * img.width;
                    htm += "<a target='_blank' href='" + data[i] + "'><img style='width: " + width + "px;height: " + height + "px;' src='" + data[i] + "'/></a>";
                }
                $(".upload-img-show-box:visible").append(htm);
                // 清空上传框的文件信息
                var file = document.getElementById('files');
                file.value = '';
            }
        });
    });
    $("#files").change(function () {
        var fileInput = $('#files').get(0).files[0];
        console.info(fileInput);
        if (fileInput) {
            $("#uploadimgBtn").click();
        }
    });
}