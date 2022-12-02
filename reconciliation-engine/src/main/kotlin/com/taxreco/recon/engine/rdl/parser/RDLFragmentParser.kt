package com.taxreco.recon.engine.rdl.parser

import com.taxreco.recon.engine.model.ReconciliationSetting

interface RDLFragmentParser {
    fun parse(lineNo: Int, line: String, setting: ReconciliationSetting): List<RDLParseError>
}