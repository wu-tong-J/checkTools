function loadTempldate(tmplUrl, dataUrl) {
    if (dataUrl) {
        axios.all([axios.get(tmplUrl), axios.get(dataUrl)])
            .then(axios.spread(function (tmpl, data) {
                var template = Handlebars.compile(tmpl.data)
                $('.content-wrapper').html(template(data.data));
            }));
    } else {
        axios.get(tmplUrl)
            .then(function (res) {
                $('.content-wrapper').html(res.data);
            })
    }

}

router = new Navigo(null, true);
router.on({
    '/1': function () {
        loadTempldate('/templates/config.html', "config/getConfig");
    },
    '/2': function () {
        loadTempldate('/templates/list.html');
    },
    '/3': function () {
        loadTempldate('/templates/volList.html')
    },
    '/4': function () {
        loadTempldate('/templates/fileList.html')
    },
    '/5': function () {
        loadTempldate('/templates/efileList.html')
    },
    /*'/6': function () {
        loadTempldate('/templates/sipList.html')
    },*/
    '/8': function () {
        loadTempldate('/templates/volHistoryList.html')
    },
    '/9': function () {
        loadTempldate('/templates/fileHistoryList.html')
    },
    '/10': function () {
        loadTempldate('/templates/efileHistoryList.html')
    },
    /*'/11': function () {
        loadTempldate('/templates/sipHistoryList.html')
    },*/
    '/13': function () {
        loadTempldate('/templates/sumCount.html')
    },
    '/14': function () {
        loadTempldate('/templates/sumCountCurr.html')
    },
    '/15': function () {
        loadTempldate('/templates/log.html')
    },
}).resolve();

Handlebars.registerHelper({
    ifSelect: function (v1, v2, options) {
        if (v1 != null && v1 == v2) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    }
})
