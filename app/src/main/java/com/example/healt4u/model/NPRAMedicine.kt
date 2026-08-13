package com.example.healt4u.model

data class NPRAMedicine(
    val regNo: String,              // reg_no
    val refNo: String,              // ref_no
    val product: String,            // product
    val status: String,             // status
    val description: String,        // description
    val holder: String,             // holder
    val holderOsa: String,          // holder_osa
    val manufacturer: String,       // manufacturer
    val manufacturerOsa: String,    // manufacturer_osa
    val importer: String,           // importer
    val importerOsa: String,        // importer_osa
    val dateReg: String,            // date_reg
    val dateEnd: String,            // date_end
    val activeIngredient: String,   // active_ingredient
    val mdcCode: String,            // mdc_code
    val genericName: String         // generic_name

        )