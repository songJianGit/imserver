// 检查a 比b 多了哪些信息
function a_b(a, b) {
    var users=[];
    for (var i = 0; i < a.length; i++) {
        var id = a[i].id;
        var bb = true;
        for (var j = 0; j < b.length; j++) {
            var idd = b[j].id;
            if (id == idd) {
                bb = false;
                break;
            }
        }
        if(bb){
            users.push(a[i]);
        }
    }
    return users;
}