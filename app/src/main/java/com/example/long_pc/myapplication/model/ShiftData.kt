package com.example.long_pc.myapplication.model

import jp.drjoy.app.domain.model.shift.Memo
import jp.drjoy.app.domain.model.shift.Shift

class ShiftData {
    var departmentId: String? = null
    var isPublished: Boolean? = null
    var memos: ArrayList<Memo>? = null
    var shifts: ArrayList<Shift>? = null
}