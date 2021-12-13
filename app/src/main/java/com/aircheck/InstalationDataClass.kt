package com.aircheck

data class InstallationData(
    val current: Current,
    val forecast: List<Forecast>,
    val history: List<History>
)

data class Current(
    val fromDateTime: String,
    val indexes: List<Indexes>,
    val standards: List<Standard>,
    val tillDateTime: String,
    val values: List<Value>
)

data class Forecast(
    val fromDateTime: String,
    val indexes: List<Indexes>,
    val standards: List<Standard>,
    val tillDateTime: String,
    val values: List<Value>
)

data class History(
    val fromDateTime: String,
    val indexes: List<Indexes>,
    val standards: List<Standard>,
    val tillDateTime: String,
    val values: List<Value>
)

data class Indexes(
    val advice: String,
    val color: String,
    val description: String,
    val level: String,
    val name: String,
    val value: Double
)

data class Standard(
    val averaging: String,
    val limit: Double,
    val name: String,
    val percent: Double,
    val pollutant: String
)

data class Value(
    val name: String,
    val value: Double
)