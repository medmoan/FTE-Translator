package farsi.toenglish.translation.uoi.fragments

import farsi.toenglish.translation.utils.UnityAdsUtil


fun displayInterstitial(ad: UnityAdsUtil?, onNavigate: () -> Unit = { }){

    // Displaying interstitial ad, in case of not loaded ad do an action which I want
    // In case of null {admob} variable do an action, invocable because it is not passed as
    // an argument but in case our variable is null

    ad?.showInterstitial(onNavigate) ?:onNavigate()
}