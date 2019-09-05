var unit = avalon.define({
    //id必须和页面上定义的ms-controller名字相同，否则无法控制页面
    $id: "unit",
    datalist: {},
    textunit: "请求数据",

    request: function () {
        $.ajax({
            type: "post",
            url: "/zky/unitdata",    //向后端请求数据的url
            data: {},
            success: function (data) {
                $('button').removeClass("btn-primary").addClass("btn-success");

                unit.dataunitlist = data;

                unit.textunit = "数据请求成功，已渲染";
            }
        });
    }
});
