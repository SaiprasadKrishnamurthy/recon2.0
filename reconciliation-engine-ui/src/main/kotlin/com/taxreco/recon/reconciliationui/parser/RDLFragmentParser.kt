package com.taxreco.recon.reconciliationui.parser

import com.taxreco.recon.reconciliationui.model.ReconciliationSetting

interface RDLFragmentParser {
    fun parse(lineNo: Int, line: String, setting: ReconciliationSetting): List<RDLParseError>
}