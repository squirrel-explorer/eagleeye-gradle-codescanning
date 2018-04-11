package com.squirrel_explorer.eagleeye.codescanning

interface AnalysisCallback {
    void onRulesDisabled(Set<String> disable)
    void onRulesEnabled(Set<String> enable)
    void onRulesChecked(Set<String> check)

    void onTextReport(File textReporter)
    void onHtmlReport(File htmlReporter)
    void onXmlReport(File xmlReporter)

    void onCustomRulesAdded(Set<String> customJars)
}
