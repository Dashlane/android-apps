package com.dashlane.ext.application

import androidx.annotation.VisibleForTesting
import com.dashlane.core.helpers.AppSignature
import com.dashlane.url.UrlDomain
import com.dashlane.url.findAllChildrenOf
import com.dashlane.url.toUrlDomain
import com.dashlane.url.toUrlDomainOrNull

interface KnownApplication {
    

    val packageName: String

    

    val signatures: AppSignature?

    

    val mainDomain: String?

    

    fun isSecureToUse(url: String?): Boolean

    

    fun canOpen(url: String?): Boolean

    data class AutofillBlackList(
        override val packageName: String
    ) : KnownApplication {
        override val signatures: AppSignature? = null
        override val mainDomain: String? = null

        

        override fun isSecureToUse(url: String?): Boolean {
            return false
        }

        

        override fun canOpen(url: String?): Boolean {
            return false
        }
    }

    data class Browser(
        override val packageName: String,
        override val signatures: AppSignature?
    ) : KnownApplication {
        override val mainDomain: String? = null

        

        override fun isSecureToUse(url: String?): Boolean {
            return true
        }

        

        override fun canOpen(url: String?): Boolean {
            return false
        }
    }

    data class App(
        override val packageName: String,
        override val signatures: AppSignature?,
        

        val mainUrlDomain: UrlDomain,
        

        private val linkedDomainsId: String? = mainUrlDomain.let {
            KnownLinkedDomains.getMatchingLinkedDomainId(mainUrlDomain)
        },
        

        val keywords: Set<String>?,
        

        val commonUsedUrlDomains: Set<UrlDomain>?,
        

        val popularApp: Boolean = false
    ) : KnownApplication {
        

        override val mainDomain: String = mainUrlDomain.value

        

        override fun isSecureToUse(url: String?): Boolean {
            val urlDomainToAccept =
                url?.toUrlDomainOrNull() ?: return false 

            return matchMainDomain(
                mainUrlDomain,
                urlDomainToAccept
            ) || matchCommonUsedUrlDomains(urlDomainToAccept) || matchLinkedDomains(urlDomainToAccept)
        }

        private fun matchLinkedDomains(urlDomainToAccept: UrlDomain): Boolean {
            return linkedDomainsId?.let {
                KnownLinkedDomains.getMatchingLinkedDomainId(urlDomainToAccept) == it
            } ?: false
        }

        private fun matchCommonUsedUrlDomains(urlDomainToAccept: UrlDomain): Boolean =
            commonUsedUrlDomains?.findAllChildrenOf(urlDomainToAccept)?.isNotEmpty() ?: false

        private fun matchMainDomain(knownAppMainDomain: UrlDomain, urlDomainToAccept: UrlDomain): Boolean =
            knownAppMainDomain.root == urlDomainToAccept.root

        

        override fun canOpen(url: String?): Boolean {
            val urlDomainToAccept =
                url?.toUrlDomainOrNull() ?: return false 

            return matchMainDomain(mainUrlDomain, urlDomainToAccept) || matchLinkedDomains(urlDomainToAccept)
        }
    }

    
    private sealed class Signature {

        abstract fun toAppSignature(packageName: String): AppSignature?

        class Sha256(val signatures: Array<out String>) : Signature() {
            override fun toAppSignature(packageName: String) =
                AppSignature(packageName, sha256Signatures = signatures.toList())
        }

        class Sha512(val signatures: Array<out String>) : Signature() {
            override fun toAppSignature(packageName: String) =
                AppSignature(packageName, sha512Signatures = signatures.toList())
        }

        object NoSignature : Signature() {
            override fun toAppSignature(packageName: String): AppSignature? = null
        }
    }

    companion object {

        private val noSignature = Signature.NoSignature

        @VisibleForTesting
        internal val knownAppsList = listOf(
            browser(
                "com.android.chrome",
                sha256("f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83")
            ),
            browser(
                "com.chrome.beta",
                sha256("da633d34b69e63ae2103b49d53ce052fc5f7f3c53aab94fdc2a208bdfd14249c")
            ),
            browser(
                "com.chrome.canary",
                sha256("2019dfa1fb23efbf70c5bcd1443c5beab04f3f2ff4366e9ac1e3457639a24cfc")
            ),
            browser(
                "com.microsoft.emmx",
                sha256("01e1999710a82c2749b4d50c445dc85d670b6136089d0a766a73827c82a1eac9")
            ),
            browser(
                "com.opera.browser",
                sha256("5d6afbf87f652af04647ada0df634cf22370900b164b09d50bd23aa2cb5285b8")
            ), 
            browser(
                "com.opera.mini.native",
                sha256("57acbc525f1b2ebd19196cd6f014397cc910fd18841e0ae850febc3e1e593ff2")
            ),
            browser(
                "com.opera.touch",
                sha256("aad8e204d24d177934c9cd0c63cc6aa38efbf42c4a6957097e2210f57faa67aa")
            ),
            browser(
                "com.sec.android.app.sbrowser",
                sha256("34df0e7a9f1cf1892e45c056b4973cd81ccf148a4050d11aea4ac5a65f900a42")
            ), 
            browser(
                "org.mozilla.firefox",
                sha256("a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04")
            ),
            browser(
                "org.mozilla.fenix",
                sha256("5004779088e7f988d5bc5cc5f8798febf4f8cd084a1b2a46efd4c8ee4aeaf211")
            ),
            browser(
                "org.mozilla.focus",
                sha256("6203a473be36d64ee37f87fa500edbc79eab930610ab9b9fa4ca7d5c1f1b4ffc")
            ),
            browser(
                "com.duckduckgo.mobile.android",
                sha256("bb7bb31c573c46a1da7fc5c528a6acf432108456feec50810c7f33694eb3d2d4")
            ),
            browser(
                "com.brave.browser",
                sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
            ),
            browser(
                "com.google.android.googlequicksearchbox",
                sha512("edf99db872937471eb94cbe576512a0089527e28b5b65df96f18f539737955ef1ce2553a51156ee31b521dcdc1559c52e965899f13038487d03743742b634326")
            ),
            browser("com.chrome.dev", sha256("9044ee5fee4bbc5e21dd44665431c4eb1f1f71a32716a0bc927bcbb39233cabf")),
            browser(
                "com.ecosia.android",
                sha256("f522d5df0ab592168bdee6151413fd98022722f7f2f85efe72b81e28ddec765c")
            ),
            browser(
                "com.kiwibrowser.browser",
                sha256("c069ea966332e91e0a0c3cc577e6f509fe3d9ddaf7015ad0c5c5ef8fda3f8628")
            ),
            browser(
                "com.sec.android.app.sbrowser.beta",
                sha256("34df0e7a9f1cf1892e45c056b4973cd81ccf148a4050d11aea4ac5a65f900a42")
            ), 
            browser(
                "com.opera.browser.beta",
                sha256("5d6afbf87f652af04647ada0df634cf22370900b164b09d50bd23aa2cb5285b8")
            ), 
            browser(
                "com.opera.mini.native.beta",
                sha256("57acbc525f1b2ebd19196cd6f014397cc910fd18841e0ae850febc3e1e593ff2")
            ),
            browser(
                "org.mozilla.firefox_beta",
                sha256("a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04")
            ),
            browser(
                "com.brave.browser_beta",
                sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
            ),
            browser(
                "com.brave.browser_nightly",
                sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
            ),
            browser(
                "com.yandex.browser",
                sha256("aca405ded8b25cb2e8c6da69425d2b4307d087c1276fc06ad5942731ccc51dba")
            ),
            browser(
                "com.yandex.browser.alpha",
                sha256("aca405ded8b25cb2e8c6da69425d2b4307d087c1276fc06ad5942731ccc51dba")
            ),
            browser(
                "com.vivaldi.browser",
                sha256("e8a78544655ba8c09817f732768f5689b1662ec4b2bc5a0bc0ec138d33ca3d1e")
            ),
            browser(
                "com.vivaldi.browser.snapshot",
                sha256("e8a78544655ba8c09817f732768f5689b1662ec4b2bc5a0bc0ec138d33ca3d1e")
            ),
            app(
                "com.facebook.katana",
                sha256("e3f9e1e0cf99d0e56a055ba65e241b3399f7cea524326b0cdd6ec1327ed0fdc1"),
                true,
                "facebook.com",
                null,
                setOf("instagram.com")
            ),
            app("com.amazon.windowshop", noSignature, true, "amazon.com"),
            app(
                "com.linkedin.android",
                sha256("3b6a403ca91d56ab854517720203176640816d3f923f92cca16138f5cc3d0727"),
                true,
                "linkedin.com"
            ),
            app("com.pinterest", noSignature, true, "pinterest.com"),
            app(
                "com.google.android.gm",
                sha256("f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83"),
                true,
                "google.com",
                setOf("gmail"),
                setOf("gmail.com")
            ),
            app(
                "com.twitter.android",
                sha512("bb403e3bb22fba89239ea99fd5c89a8328b0931aac2d2ad703d673c886f8c86115bb9b4f45f8496e7f111071a60c052450219fef54eac1473049fc0539b46cb0"),
                true,
                "twitter.com"
            ),
            app(
                "com.yahoo.mobile.client.android.mail",
                sha512("ed2bc72f3baae2046698d979317593c3da9498f0a274d17db95cec07a02e28dfcadb5c2c34f799027914498bdbf60ca30be9c6375b20a545ba71d29ff0f81b5d"),
                true,
                "yahoo.com"
            ),
            app("com.dropbox.android", noSignature, true, "dropbox.com"),
            app("com.evernote", noSignature, true, "evernote.com"),
            app("com.duolingo", noSignature, true, "duolingo.com"),
            app("com.ninegag.android.app", noSignature, false, "9gag.com"),
            app("com.backelite.vingtminutes", noSignature, false, "20minutes.fr"),
            app("com.airbnb.android", noSignature, true, "airbnb.com"),
            app("com.sand.airdroid", noSignature, false, "airdroid.com"),
            app("com.allocine.androidapp", noSignature, true, "allocine.fr"),
            app("com.eclaps.alloresto", noSignature, false, "alloresto.fr"),
            app("fm.last.android", noSignature, true, "last.fm"),
            app("com.lemonde.androidapp", noSignature, false, "lemonde.fr"),
            app("com.linxo.androlinxo", noSignature, false, "linxo.com"),
            app("com.ideashower.readitlater.pro", noSignature, true, "getpocket.com"),
            app("com.soundcloud.android", noSignature, true, "soundcloud.com"),
            app("com.spotify.music", noSignature, true, "spotify.com"),
            app("com.lafourchette.lafourchette", noSignature, true, "lafourchette.com"),
            app("tunein.player", noSignature, true, "tunein.com"),
            app("com.delicious", noSignature, false, "delicio.us"),
            app("fr.amazon.mShop.android", noSignature, false, "amazon.fr"),
            app("uk.amazon.mShop.android", noSignature, false, "amazon.co.uk"),
            app(
                "com.bankeen",
                sha256("fc5c8fe615059211f7f70b75da0ba7dc52842c2ec1a59297e0a6b11a9ca4c54e"),
                true,
                "bankin.com"
            ),
            app("com.dailymotion.dailymotion", noSignature, true, "dailymotion.com"),
            app("deezer.android.app", noSignature, true, "deezer.com"),
            app("com.groupon", noSignature, true, "groupon.com"),
            app("com.hipmunk.android", noSignature, true, "hipmunk.com"),
            app("com.hootsuite.droid.full", noSignature, true, "hootsuite.com"),
            app("com.huffingtonpost.android", noSignature, true, "huffingtonpost.com"),
            app(
                "com.imdb.mobile",
                sha256("6a87e5e44564d487b1b759befb32c8c284ed0aa8ed52a277aedcad79de28a50d"),
                true,
                "imdb.com"
            ),
            app(
                "com.lastpass.lpandroid",
                sha256("e5de0a7f60fc5ed119c7952b2540f9006e6ab4a4b27f8cadb9f4164bb9c91b71"),
                false,
                "lastpass.com"
            ),
            app("com.kreactive.leparisienrssplayer", noSignature, false, "leparisien.fr"),
            app("com.orange.mail.fr", noSignature, false, "orange.fr"),
            app("com.mailchimp.mailchimp", noSignature, true, "mailchimp.com"),
            app("com.aufeminin.marmiton.activities", noSignature, true, "marmiton.org"),
            app("com.marvel.comics", noSignature, true, "marvel.com"),
            app(
                "com.nytimes.android",
                sha512("a4313891f98b6958aea2c3512171914b68b825a4721878f5919b7b2c1958596570fd7190c66104652494d991006f1af7ce9e61b74b4736dd5341a3e404d19684"),
                true,
                "nytimes.com"
            ),
            app("com.newsblur", noSignature, true, "newsblur.com"),
            app("com.outlook.Z7", noSignature, true, "outlook.com"),
            app("com.path", noSignature, true, "path.com"),
            app(
                "com.paypal.android.p2pmobile",
                sha256("c7c62ea0f6e2f6eba87f95406987505530ef2f516e7cde5991d7140331448072"),
                true,
                "paypal.com"
            ),
            app("fr.taxisg7.grandpublic", noSignature, false, "taxig7.fr"),
            app("com.producteev.android", noSignature, true, "producteev.com"),
            app("com.quora.android", noSignature, true, "quora.com"),
            app("com.runtastic.android", noSignature, true, "runtastic.com"),
            app("com.sfr.android.sfrmail", noSignature, false, "sfr.fr"),
            app(
                "com.skype.raider",
                sha256("7d539351ca39c27ca706409e5a9b6b062db9bf8dc3d8caa6137067ae7f68b5e7"),
                true,
                "skype.com"
            ),
            app(
                "com.valvesoftware.android.steam.community",
                sha512("7cd5ddda23e1b0b034685d928c5337afc9c304b919d327da5d42dc3e25efaf9939f5f6cdc158d9cb9e7fe1dd9505dc56f17880e866ff2e2e33c0fc656486bc2e"),
                true,
                "steampowered.com"
            ),
            app(
                "com.valvesoftware.steamlink",
                sha256("5dff6b05761447a5bdf919ea88fc6fdf20d301e30b2315415c4d368ec0fbda45"),
                false,
                "steampowered.com"
            ),
            app(
                "com.valvesoftware.underlords",
                sha256("be66e057cd456a415ac80e1e101f560a8c5a4a43fc94ac638090ff74fe5e00ca"),
                false,
                "steampowered.com"
            ),
            app(
                "com.valvesoftware.android.steam",
                sha256("5dff6b05761447a5bdf919ea88fc6fdf20d301e30b2315415c4d368ec0fbda45"),
                false,
                "steampowered.com"
            ),
            app("com.stumbleupon.android.app", noSignature, true, "stumbleupon.com"),
            app("com.trello", noSignature, true, "trello.com"),
            app(
                "com.tumblr",
                sha256("39ac9c821ad2d605e4849f066e8aad3cca27d5e13eb03ad8755be234a2985dfd"),
                true,
                "tumblr.com"
            ),
            app("com.venteprivee", noSignature, false, "vente-privee.com"),
            app("com.viadeo.android", noSignature, true, "viadeo.com"),
            app("vdm.activities", noSignature, false, "viedemerde.fr"),
            app("com.vimeo.android.videoapp", noSignature, true, "vimeo.com"),
            app(
                "com.vsct.vsc.mobile.horaireetresa.android",
                noSignature,
                true,
                "voyages-sncf.com"
            ),
            app("com.withings.wiscale", noSignature, true, "withings.com"),
            app("com.wunderkinder.wunderlistandroid", noSignature, true, "wunderlist.com"),
            app("com.yammer.v1", noSignature, true, "yammer.com"),
            app("com.youmag.android", noSignature, false, "youmag.com"),
            app("flipboard.app", noSignature, true, "flipboard.com"),
            app("com.citiuat", noSignature, true, "citibank.com"),
            app("com.clairmail.fth", noSignature, false, "53.com"),
            app("com.wsod.android.tddii", noSignature, false, "tdbank.com"),
            app("com.tapatalk.discussamtraktrainscom", noSignature, true, "amtrak.com"),
            app("com.prosoftnet.android.idriveonline", noSignature, true, "idrive.com"),
            app("com.venteprivee.levoyage", noSignature, false, "vente-privee.com"),
            app("com.etrade.tabletapp", noSignature, false, "etrade.com"),
            app("com.lepass.android", noSignature, false, "vente-privee.com"),
            app("com.capitainetrain.android", noSignature, true, "trainline.com"),

            app(
                "com.Slack",
                sha512("a0cfd1f8ebdb8178dd3d0e0c4722e4f2697726c97401c25cae2ff56e66785503767957fb111dd4747ec88bc31a3c3538157778e1058d5203c3b23a010b7ed8bd"),
                true,
                "slack.com"
            ),
            app("com.tencent.mobileqq", noSignature, true, "qq.com"),
            app(
                "fr.cnamts.it.activity",
                sha256("11b6a8b319fced5a3efc880bb373bd8158cb3c7ff83df2051fe2a36013e8a1d9"),
                false,
                "ameli.fr"
            ),
            app("com.guerdons.passresto", noSignature, false, "sodexo.com"),
            app(
                "com.ebay.mobile",
                sha256("85b4015497154dfa89de308771a9fc61a64b19af1a3fb03256e8cc3a795567e5"),
                true,
                "ebay.com",
                null,
                setOf("paypal.com")
            ),
            app(
                "com.wf.wellsfargomobile",
                sha256("6a2d2b7689beb759b815d4aa582708f61e7932bb1a2b8213e4cf1fc3d8ec4c5e"),
                true,
                "wellsfargo.com"
            ),
            app(
                "com.mcdonalds.app",
                sha256("392db11ddc91594b2bc7c0b05d75175f466c3495c7345a0bcd77df70380458b0"),
                false,
                "mcdonalds.com"
            ),
            app(
                "com.mcdo.mcdonalds",
                sha256("08bbcb26586a81cbcad3df4fe804be08e24d1affb1379627d96cadabf1d4529b"),
                false,
                "mcdonalds.com"
            ),
            app(
                "com.ubercab",
                sha256("4003231ab42feb8d955e62fdfaa4fad59567054b9c6bbc3d950299ddfc268392"),
                true,
                "uber.com"
            ),
            app(
                "com.chase.sig.android",
                sha512("6799cde91a68fca19dd899d54ab5767aaa3d915eb7f5f3b346f0e9574177d7fa936b62ee788693f06a36ab647b2f184648eb5c21f0d88967e181bf885a568b61"),
                true,
                "chase.com"
            ),
            app(
                "com.konylabs.capitalone",
                sha512("ddb5185796ba49bfe5912668e21a1fc292174f899166e1f79730a80012fcd6140acde441f2946781d90d5ac84ace331a3a052af7ea60872ed12de0535b329726"),
                false,
                "capitalone.com"
            ),
            app(
                "com.citi.citimobile",
                sha512("190a87c745e38f46ab9852fdfe8599fd0367e3befd788b5c54cacf19cc9af2a96ffbd531805ba965d5600b9427fe3d73c5de564521247a83a61aadb5e055a658"),
                false,
                "citi.com"
            ),
            app(
                "de.number26.android",
                sha512("295716704609d894e2333dfe2eff68f3c4cbe67d359bfc10e6315c0ce1e60276de7fcb70c3543e427ecadabadd52051f3214e89ed077df2873f4c020d9805e21"),
                false,
                "n26.com",
                setOf("n26")
            ),
            app(
                "com.vanguard",
                sha512("c45b44253af1d16a02cfbae228596ce05b125cceb7b550244900f704dab8ee4a19ccdcf64eaa4dc91f6afb4558ec802134bfe0401f7f0d268cc90621b9f645a2"),
                false,
                "vanguard.com"
            ),
            app(
                "com.nordvpn.android",
                sha512("059e35c38725cb7ebfda0d479aeec73bf300d42c52b9046756f87b2fdd877df8bf6dfb39c9abf0a19f2decd166190bdd3bbb7948f60b45d8f39d59f0b70eb4c2"),
                false,
                "nordvpn.com"
            ),
            app(
                "com.fidelity.android",
                sha512("cbc45c6727cbaf5e7eee6ae8ad0f08d405db119d5fe780fdc21e6fc4addca16ed62896e593fda0649bd038b292af2c2ba226cb1dfc89641654ad21f675008705"),
                false,
                "fidelity.com"
            ),
            app(
                "com.microsoft.office.outlook",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                true,
                "live.com"
            ),
            app(
                "com.rbc.mobile.android",
                sha512("f16c9682c628ca2b9ddea9b20cec46f7253a25bfd3a015f6a07fb45958806fb822e3f18c9d58b189dac224d8cd24e733c11595322180ae9d01d00be62a0f3dab"),
                false,
                "royalbank.com"
            ),
            app(
                "com.schwab.mobile",
                sha512("f68e2af57addd9b382d6e3c9948421bdb43eaeaea416787e2f2af3ad73114582084b7523a5e5d1530e41a5b4cf87e36c474eb1ffea73524d885108dd26a530eb"),
                false,
                "schwab.com"
            ),
            app(
                "com.instagram.android",
                sha512("a9b31009987e094fd2067d385056adfdb2cc4a272814a5982343335e639d064943e6025cbc7e36903f065c0e65bd99adab538dd0377c1404fa48b508970354a8"),
                true,
                "instagram.com",
                null,
                setOf("facebook.com")
            ),
            app(
                "com.americanexpress.android.acctsvcs.us",
                sha512("bd0e86334e5d465c7a0dafc0cbcb217c739f37c6b5cdabcd51993f9d03c8b63f8b9985ffb6288a7711b1fc934e6167c763908c0a8efd3cc3e182892cc821bdc9"),
                false,
                "americanexpress.com"
            ),
            app(
                "com.discord",
                sha512("0675fd33ae8a99aaa6ce0a96810c666eb241ff03b4493e77f835190a7c4374bc1fe0902c52d33690a1ae76378172524552ab803f6253a72a64f6aa1b03a3c630"),
                false,
                "discordapp.com"
            ),
            app(
                "com.costco.app.android",
                sha512("4b3ba61a5c0905ee94348034309095b26f8ec67560b33f7c0ec4925ee353f526a12f62c9575555db8a58eccec052586de82afba3c18678f923c05002121b0d57"),
                false,
                "costco.com"
            ),
            app(
                "com.bmo.mobile",
                sha512("bdcc381f8e26bebe184d95ee7dda2ec99252752599b5a61fe2a2b6823cf6565ac1c2c8708c1aaef670e154e67c28e4d7dd2e0565c2b29fd55c969073260c93f3"),
                false,
                "bmo.com"
            ),
            app(
                "com.azure.authenticator",
                sha512("1aef02b9a626495e421d677a773ded18f5c813e6136a50952178b9944057a6f520b0c2a81db53d46aa2edd6351354d6db33f29bc40034c2089e5fd367dbc3dab"),
                false,
                "live.com"
            ),
            app(
                "com.scee.psxandroid",
                sha512("645acd5383c971d39c88b1ecfaa6929cdd4e09c79ab2dd89d4f8b67148dc98f6ed6ce577551c79f83211cde61c506ccc5dc723a7c5944530c2460aafeef4e107"),
                false,
                "sonyentertainmentnetwork.com"
            ),
            app(
                "com.pnc.ecommerce.mobile",
                sha512("3a94fdf0371d6020339106b9c99a4d2f9d4698a99405b64a3d02bd78718b899ae4333ff120d71d4d57b003ff508c574aa1e2729181778fec488cafe6bde4fef7"),
                false,
                "pnc.com"
            ),
            app(
                "com.etrade.mobilepro.activity",
                sha512("6213d168280eade342e9de545d99fc9fbf3385579ce8d111edd314fbb194eee4375e7cd8b47262ddd5880c6366cc01c302c890b2df7a2ebd9b01a4af5070f00b"),
                false,
                "etrade.com"
            ),
            app(
                "com.microsoft.skydrive",
                sha512("1596fa1a80ed464500c884463e62bf12556f59f85122b27ee9ae35613cfd627cbdb224ee204b912a7e0334d4d4f9625f01233df74b02dedb3d73aa2aa3e5efb8"),
                false,
                "live.com",
                null,
                setOf("skydrive.com")
            ),
            app(
                "com.samsung.android.mobileservice",
                sha512("0018b67dbb7cbe4ce3ed227c683e63738c491530c59ed76432b6172f78adb2fa98d50230f9d668cdda29e6b80a3718dbad001de679ed006a6087dc3ce0ed5782"),
                false,
                "samsung.com"
            ),
            app(
                "com.amazon.dee.app",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.regions.mobbanking",
                sha512("94143e3f4fba210c64d5dc266a6b65284bdef03db1bffc6fad0ba45d6f604c17f4415c4381c1fa7a45244a8e8acfcd02b2b3d915e26c3b64355118c84cf76327"),
                false,
                "regions.com"
            ),
            app(
                "com.hulu.plus",
                sha512("1ccb09886fdc9eb06ce0a8dc944cd8bd0a78a93af7a80b199da5d0225a71bdc9903ca74ff29b233d68f334b14889f7c800cc2c460ae4fc76c0722300fa002eb4"),
                false,
                "hulu.com"
            ),
            app(
                "de.postbank.finanzassistent",
                sha512("6434d585a5d91118fd8f1d5226ddd032fb7d9f71c26c66e14030be0041f2f7a710a91f1305ade57d815863ae2c72f6fca23d1c5fa665a6ef730c2eb39ce00e68"),
                false,
                "postbank.de"
            ),
            app(
                "com.barclaycardus",
                sha512("029e1f9cdf5df5638a732290dd420972c677137e82a095934993b762e81d5875d817729df2bcbdecb23eaf9f04d51722da8f7a915707a36ed2f484b39c9db065"),
                false,
                "barclaycardus.com"
            ),
            app(
                "com.google.android.apps.adm",
                sha512("edf99db872937471eb94cbe576512a0089527e28b5b65df96f18f539737955ef1ce2553a51156ee31b521dcdc1559c52e965899f13038487d03743742b634326"),
                false,
                "google.com"
            ),
            app(
                "au.com.nab.mobile",
                sha512("6bdbd2dc331c0023bd2e10f2acddd0358b6ae018eba8b9c048b0527a7507610c265b8ed6c0c47b228bd9839a63acd0b7cb9820265ffabe09892acf3cb61c87ee"),
                false,
                "nab.com.au"
            ),
            app(
                "com.gobank",
                sha512("4ed821d499ab5d87b5df9c8600bd9aa2d0a35bca6b6e66c2abb08c17b19c7a184a1f222f91e44d5cc7688754a8a51ca42ab02fc0603af43c1456ce9aa05231ae"),
                false,
                "gobank.com"
            ),
            app(
                "com.allstate.view",
                sha512("c8f21fb024a90b6906045dc0255dda7452d7aefc6c50cfd15aae8241f7d507860b51cb2b11de4df32c48f67ed220134910f639ec0118d1f23dd6e63ac0d47959"),
                false,
                "allstate.com"
            ),
            app(
                "com.crunchyroll.crunchyroid",
                sha512("3fdd4b68d706f8474c3c6cad2df5e033b7b8c2f661ed256bcbbb1822036e70162d88bae4b7c19b95406a7a5b2d308ccf1352239a78bf9d5284cb4c6d866f638b"),
                false,
                "crunchyroll.com"
            ),
            app(
                "com.teamviewer.teamviewer.market.mobile",
                sha512("2d62dad429f8e521ec0e5bc61e2bddb5fa15c71af48f88fb482e7354f09d7bdb34c1e91b5ed161ce1bdaf80e8a8340c31c7c06dd0d8193c26bb3b43608f4f984"),
                false,
                "teamviewer.com"
            ),
            app(
                "fr.gouv.finances.smartphone.android",
                sha512("114f6609c9bf7020ae775efcc0a786c580af82c6239e3c93edd89d42ce03f9e1fd8903d38f0f8a40e043fbf3e35b3dde956d79653c82f00083f3a3652671fab0"),
                false,
                "impots.gouv.fr"
            ),
            app(
                "com.tdameritrade.mobile3",
                sha512("19bf34e1972b2bfb85065c2be4b14a190a82af51d09c03b2f584c0a76182f42368832b83a7cbedbfff93911544f1aabc1fe9088b8fe1e7fa040fa01a92cd4bd5"),
                false,
                "tdameritrade.com"
            ),
            app(
                "com.huntington.m",
                sha512("5051f964c34589fd8aa5791da29293994654239da7ad5681cf85520cac5befdf1a3b9f73b46cae86d4b940f87288496d2a02ccd998fcf4d7a8bc13d99096f4dd"),
                false,
                "huntington.com"
            ),
            app(
                "com.tdbank",
                sha512("f761b99460f0090606937a3f177a254dd399486d0024b5f99cbbe9d8708d64689fa9a09c3784cd8ef60c42e35f529c7cee6bf06242410a2f7391e5711e785015"),
                false,
                "tdbank.com"
            ),
            app(
                "com.dominospizza",
                sha512("db686bdb6c536c9e5c211d2852c047089a04174939e2f48c4f504943760468ecb744c7b484489b55f6a1bc5cdef010c8f47874b5cb95dd87b356f32636e6310d"),
                false,
                "dominos.com"
            ),
            app(
                "com.ally.MobileBanking",
                sha512("5d00f0942bf5b8aec9e9157d408f92b122ec3f935b48d88db6b8e4eda3509f6377894fc1031659c0f40e4c20710762065ed5972c5968b815b95ae4ade7e08c57"),
                false,
                "ally.com"
            ),
            app(
                "com.geico.mobile",
                sha512("59b299e05ac16a080a99c97f740cfaacbf4364214259004f48f16a596b647afbd95f0055f9b69e859162281705121f8dadd4d9eefd40ce06a2fd7cb37c508b8c"),
                false,
                "geico.com"
            ),
            app(
                "com.snapchat.android",
                sha512("af1d4cec735c94eb654db91af08a8133f440f8f3f34be86dcc7b4704941c38cda835048eb19c591758f530fb5e7242135eb27bc4a87a9840b61e6299a41c01ae"),
                false,
                "snapchat.com",
                setOf("snapchat")
            ),
            app(
                "com.teslamotors.tesla",
                sha512("2be37fb1516f47671073141b043055e1b6727d10d105a19d783396a1b725cf9786d57476bfa6eb59921b3b8efa1da7c2f636bddb0e8a6ce81b33166936b6cd68"),
                false,
                "tesla.com"
            ),
            app(
                "com.arlo.app",
                sha512("cbfc6e0da38d03a288715b31a4011c07f58829dc0a3f0229e5f5325ecf5fcebbd568db1703ce39189af35f1aa2a6085e27701ea28d663337288a56010d0d7239"),
                false,
                "arlo.com",
                null,
                setOf("netgear.com")
            ),
            app(
                "com.marriott.mrt",
                sha512("5276fc38afbae76431d314438df81201abdf83dd2368ca87b50906035062a505bec883ee00a5a42252ae21dff149a05a05e898ce650038649d89784aa7b5046e"),
                false,
                "marriott.com"
            ),
            app(
                "com.fortuneo.android",
                sha512("4d23bcb398ad5031703ff7f1fbd8eb673dcb4a5840fe3667718085aa383d7a81c5aaa87dce73aabce5c9896be33990ab56ec8c0258180940324886155299f46b"),
                false,
                "fortuneo.fr"
            ),
            app(
                "com.united.mobile.android",
                sha512("1d50bd97ac1a35963ad32bfa2a4092b5a80e7246da5fee3fcd49276557f0e2560fb1483d93e56e6914158fb39b4e14a0f7e7e5e303eda22a6d3992d169b5ec92"),
                false,
                "united.com"
            ),
            app(
                "com.concur.breeze",
                sha512("14d9d1aa1280447e454f45e100b7d3996904473a5fa19fc33d0911eecd7d2e3b9704c84f67a848b5cba8fadaa7cfa16797473a8913c0cdcbfee5f507f8e627db"),
                false,
                "concursolutions.com"
            ),
            app(
                "com.att.myWireless",
                sha512("4d2024a94e8de8343715f68cb9102d4a2ec7df27ab846251c491143e06d13749c978e46acb8bfc5693a8ba2a6b31d3dfad0725ec3adee7aea83a04bc232ae46d"),
                false,
                "att.com"
            ),
            app(
                "com.amazon.kindle",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.microsoft.appmanager",
                sha512("5a151d874e199102e635bfff94a9a8872a8374f3165c7708d0edc0be82ccb6017fb269c40f8afe560baf823e9de101bbec99776af50ab7a2dea85d9324f655ce"),
                false,
                "live.com"
            ),
            app(
                "com.ringapp",
                sha512("103f0fda03487b4cfaaf7769ef638cab595d9d2e208a0d4347e97d440cd68a671a985bf397633160dde9068e7827cf7d478b42aa028fd3dbde68d6d8cea24be3"),
                false,
                "ring.com"
            ),
            app(
                "com.ubercab.eats",
                sha512("fa557db8cdd6469572dfe542315a91ab74bc71dd674094566c8c76965c1a2d2f0e8ebfc1eea2105c2e0e7d7f1bdf524826588a8527b8c43a73d1a80d079da7b0"),
                false,
                "uber.com"
            ),
            app(
                "ch.protonmail.android",
                sha512("690c72a225bb439762331741ed312467dabf909fd42fcad356cf419c3ffb8588801ccd46e057951d9ba27b1611bbb320be8430341ea9518f825b6e9f1ded1943"),
                false,
                "protonmail.com"
            ),
            app(
                "com.audible.application",
                sha512("f12514845c59c94acb7153c2b32d12a96b14e084574327bfafd026041cb426f8601518c03a45cf22a204ffa6d0506a19a55a7b520045c9fbad62c07a68ee5d6f"),
                false,
                "amazon.com"
            ),
            app(
                "fr.leboncoin",
                sha512("e5bb03e1d3e34281ff7a20b87c7a5f739ccfcd2e0adbc9a4de1c19176fe5487af17bbda2b0954c82f57b861b38ec4b85673ad97d85fd8977210f8cdce8667715"),
                false,
                "leboncoin.fr"
            ),
            app(
                "com.sirius",
                sha512("37a06a8764154f9e5e7757319070232a31cab16ef38886627bc806efaa817378486454d1f36c65703447b67c150079d476573136c032bb3a3c4b21bfc3ea3c24"),
                false,
                "siriusxm.com"
            ),
            app(
                "com.mint",
                sha512("9e4ce4a6d0dabac73def17ab35dd48f844b28b62462d94d874a2cd909929d27ef18f5f52afa58d7b8ccb83816bf7d19a18ec96e18374e9dd6e8c9cb93506c583"),
                false,
                "mint.com",
                setOf("intuit.com")
            ),
            app(
                "fr.banquepopulaire.cyberplus",
                sha512("471a674b076d5c89184ca9a75fafe72cca587e9e9fb9d1aa1e5f16a7d3fda83bb1dfa0fa1061b31ff6d2d83a5940324e2bf449063815cf439c5ee892d2e98b62"),
                false,
                "banquepopulaire.fr"
            ),
            app(
                "com.apple.android.music",
                sha512("6246422895dbef5955980088809a2d495123d6ca10101ef10310a7b261fc50b8b0b6d24d5c82ead8fa4868140855765493940c78ef737eb2a31f11e616b8d272"),
                false,
                "apple.com"
            ),
            app(
                "com.scotiabank.banking",
                sha512("d38804bcb5f6004a9e7f3aae3fee65f583854d50142b6e0cc79e511fb03b0e138385cf4d43377277c2a38ece22cb6bd9202fb4b438b807a539693cd136e5b72f"),
                false,
                "scotiabank.com"
            ),
            app(
                "com.cm_prod.bad",
                sha512("799758e79ae8a8869c10dab86e3c6a076804743ce524b4618d3c67b636d4e42727d30f06d91458eba426070ff8c4c21cd26163871664606fb2a4d0ace0c12056"),
                false,
                "creditmutuel.fr"
            ),
            app(
                "com.app.pornhub",
                sha256("6c32eaffe709c8f58a1d511ce074e9cd736ee28ec8c067268ecab0f258bf40fe"),
                false,
                "pornhub.com"
            ),
            app(
                "com.disney.disneyplus",
                sha512("d26c45ee045b2f512bbfee8f96747cda300fea519aa274f9f28927ffa7c3762a89b921605f8695a08c8d17084f4339b6b26625b563b7ab0b868e2978eba9c121"),
                false,
                "disneyplus.com",
                setOf("disney+")
            ),
            app(
                "com.cibc.android.mobi",
                sha512("a4ca9d51a1cbc097bb2871a5cc684f57ace1b8d97f5ede3de3e5193d3488ec40f65fed5f974699d096de52bcb970325422331edfb3c20a4c17bc99854f7e3182"),
                false,
                "cibc.com"
            ),
            app(
                "com.mojang.minecraftearth",
                sha512("4af613f5f68b17d1f36ba9bd7be628c25d8e8ffccceccd7fade2fcb1020d6cc1e4e04fa332edfae232dbe623f4b24a8810ff097b7938db1e05f82703770062c1"),
                false,
                "live.com"
            ),
            app(
                "au.gov.nsw.service",
                sha512("a96ac083ac7302694be03326ea969c0014e28c6440be98353797ad5e2d4c9b0582e0b07b41d563439efc31b599c63633c23e923c486b0bb6e29315887395f693"),
                false,
                "nsw.gov.au"
            ),
            app(
                "com.mtb.mbanking.sc.retail.prod",
                sha512("f3e288b64be0e054b6a67ec10cf9e51790f7275cec63023747cbd8388115dace14d6cc991687b22804e069806b8c7a6474b92ddfd09ce0bfdb07988a77c5ae56"),
                false,
                "mtb.com"
            ),
            app(
                "com.greendot.walmart.prepaid",
                sha512("988d5771bfc9f7d9b7ed2e6ee2d51056a6ffcc88bc0438e80412f47095798dd3492a1ced512aeee9fef662a6fc9dce1df9db21b07c67e55b7f10e532084d498b"),
                false,
                "walmartmoneycard.com"
            ),
            app(
                "com.amazon.mp3",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.overdrive.mobile.android.mediaconsole",
                sha512("8a83f2c93bb9d68f061260c66b2b77a831d171491f7ae190f8742a6ec4e888fb766454d7cb20c25b668182037f681ad6fe894cdc5637df6ff24acdbeb1255ff3"),
                false,
                "overdrive.com"
            ),
            app(
                "com.md.mcdonalds.gomcdo",
                sha512("42d32566baf4a0b5a75ff90a4cd80410258dd70a64e8327fe7caa2467b50820ace70188860eabd87f41c5fdb63989d2969a7b83c28356102e34c862d0e9528df"),
                false,
                "mcdonalds.fr"
            ),
            app(
                "com.cic_prod.bad",
                sha512("799758e79ae8a8869c10dab86e3c6a076804743ce524b4618d3c67b636d4e42727d30f06d91458eba426070ff8c4c21cd26163871664606fb2a4d0ace0c12056"),
                false,
                "cic.fr"
            ),
            app(
                "com.navyfederal.android",
                sha512("e55f796aeac46cd8e9dfa835c8c827ac4a54a6307b720b70e94ddc137f264c3f037e9b24ad878e282ec32b0fe52c500169f19df679ac155798bfe8273612bb18"),
                false,
                "navyfederal.org"
            ),
            app(
                "com.mttnow.droid.easyjet",
                sha512("2155f3c0953fe54bd777a61b885dbd64ace52b5d55d6080a84864c6ea85a9127e78a8d8184b6cbc5f7fd34818e14a9299bcec57ff0785d4177e0968c62096b30"),
                false,
                "easyjet.com"
            ),
            app(
                "com.intuit.quickbooks",
                sha512("76418b69762b49dfc106924b1f725f99eeaec2f3c96f372bec60a83b6b167af5c62478516687424994f1efb3ae9fcb9eec19565de03829df291801d2e228323c"),
                false,
                "intuit.com"
            ),
            app(
                "com.bitdefender.security",
                sha512("d36cc33c5ed5c451ed373d0e20c84ee4245e0fc8a0ae3f02df052312f5a0bce3c545524cc27a68d1b4f8ef169f549b272a0f09956b22c61d2c52de7eae446b48"),
                false,
                "bitdefender.com"
            ),
            app(
                "com.bbt.myfi",
                sha512("38225a98b795a4df10c7f405e716304ecae7856973909247e5ee47fb28ea46f66df2f2deecdc0a41ac01c4efc137039e17235cdf807373f6dcf9329e8ffb99e4"),
                false,
                "bbt.com"
            ),
            app(
                "com.microsoft.teams",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "microsoftonline.com"
            ),
            app(
                "org.becu.androidapp",
                sha512("4a9d2a3eb74ceac07896916ee094f53790594203001acb4024da0c2169c1382496a29f2d8ee5f933cf2e40073c73dd997f8b8b8a3013db309c628bcdb425cd76"),
                false,
                "becu.org"
            ),
            app(
                "com.fanduel.sportsbook",
                sha512("9be58f81ee3178a9637a7120206ffe57611be3383de8dc2dd29012bc56f047fa500119287a954f38ddf7d759e9a1449d857ac5cb07583df3b7bdc3150f7c315c"),
                false,
                "fanduel.com"
            ),
            app(
                "com.firstdirect.bankingonthego",
                sha512("9f501fde9ba91b9a946de2ad278f220340826907507cdd514447e9b46494daeef3f88d2849524c3d196e89accb7d250e0a75b2e54763d749d9f88634a61c7315"),
                false,
                "firstdirect.com"
            ),
            app(
                "com.bmoharris.digital",
                sha512("95ed54daa44f1f1103cbd080ba00fd1654cc4acd8f3c753fe54f6d0c4024a2b6c2eedefab3cd3bb2bafc7cdcf3cc9d8a03741b396b574cdb38594dc2eddcc5a7"),
                false,
                "bmoharris.com"
            ),
            app(
                "com.adobe.lrmobile",
                sha512("d5fd0267448cc2657fe731b3bc25cd60a3b08153d096ff7376a0f0c97a87af35c8b810debbb86401b615a59163979e6147fb8965bce040d508458ce38e57878b"),
                false,
                "adobe.com"
            ),
            app(
                "com.statefarm.pocketagent",
                sha512("ef5b49bb6e9357eb9499de2d9d047568f49069ca1502842a31f2b43c6eb19f0dc35b506572795460e108a0ee93c37df979a02ee67814f4be85c0418da1dce0de"),
                false,
                "statefarm.com"
            ),
            app(
                "com.mcom.firstcitizens",
                sha512("45cf66ee846fa3f0389f7159c52df35f2643107b2d7a5c826709446ac5574c13e6fa75af617c765bce9ce6a785d226789abafcc7435a2e0c75bc0f0fcd07811a"),
                false,
                "firstcitizens.com"
            ),
            app(
                "com.globalegrow.app.gearbest",
                sha512("7820d221a2e9fac08414df28079502d288096ec22990fa0193fee52a93be2dcd1e5ca06e9e959c323a3950b725a7d7a5a402748708a1645f7f505ab3850f3e5c"),
                false,
                "gearbest.com"
            ),
            app(
                "com.microsoft.office.onenote",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "live.com"
            ),
            app(
                "com.bitlove.fetlife",
                sha512("5fa58ae43ccd80173c40c007327b62b7049145bf4ee96139eacb3224f2d468d20e838369233926d3e8d5892b50ea6c811c30fd8e92c48547e00b7edb5b356926"),
                false,
                "fetlife.com"
            ),
            app(
                "com.quicken.qm2014",
                sha512("29df2a86fd38dabe947609327365d64e51c4a416b975f81a271948e23894029487bf08e60a601fd8d7142beba2fa6eef434898ff3df366a7197cd85978a36cd3"),
                false,
                "quicken.com"
            ),
            app(
                "com.brocelia.cgp",
                sha512("8ae4861397bc5370d0765576251e20cfe592c62c34750c430037edb890a096b7d8a9f68f0b0ef1107cb4731dffbab19fdd4c1c6554033fdac8c7aa606233aed9"),
                false,
                "cinemaspathegaumont.com"
            ),
            app(
                "fr.fnac.com",
                sha512("4f8a375e9d8639d6e860fe680dfdc0d5ddf3021ff52c3ee02060b98b91037591b1876ae7113f75be3aa4469a6cee0f01f7328e17cfb8fdcd6fdfabf36b7e0ee8"),
                false,
                "fnac.com"
            ),
            app(
                "org.kp.m",
                sha512("afa73f7c2172555c67090fbe6f0b2b882c5d0b0d3c5c096ff13c289eef6211867938f5f7b36aa3dff3e4761cc5dad55f9d2993b918c93c8e86d5f154a5081585"),
                false,
                "kp.org"
            ),
            app(
                "com.docusign.ink",
                sha512("0657b1ce10ac4522705dafe827d23ad4b4fbf7e521b561182746c1975c25c95cb6a46504fd11e53137cbbefbd1c1f08c71cb708f7ca08a2d50d98936e4be0890"),
                false,
                "docusign.com"
            ),
            app(
                "com.mobile.lakemichigancu",
                sha512("f6bba9f88db920fe71f71a1cbe147793c421cbc18e6d7410c62e010857bf0198d41d508d013b7694172a946a51d951132710d182e456b3b7ee9365862be071a3"),
                false,
                "lmcu.org"
            ),
            app(
                "de.comdirect.android",
                sha512("0f790502bba7c3bc08fcff8ce9944632c374f4eda9d15887c334f83782bc89293d8975d2797aa00df76f58bd2269e47c16a394f2d2a0b1598d298d00a5a8519a"),
                false,
                "comdirect.de"
            ),
            app(
                "com.mstv.bd.prod",
                sha512("c8fc9344a30345fd880da3ed6e8b7bcce2932427e9467c8d89ae7cfbf465c5d4c2ae2033619ddf27187bc62febcb3c77d8c147c924b7595905b5e9e810fcc8d8"),
                false,
                "boursedirect.fr"
            ),
            app(
                "com.aetna.aetnahealth",
                sha512("b6e8d918e3f59a68cb319760f9af3b11455583fcc69f1b4603030889b3f34a7cbee5decfa5d797f277d72ab36870705a8721ceb098a8ee5e958643e4de7c3876"),
                false,
                "aetna.com"
            ),
            app(
                "com.amazon.mShop.android.shopping",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.netgear.netgearup",
                sha512("cc58616dad6cb2e11d8c3809c4c6e841e0e2a8673aa7583548a4792d5e0c3a44119e08973737e1d20bf7ae54b4196139ab3ef87f2b5d0c27dd533369c3b02ae9"),
                false,
                "netgear.com"
            ),
            app(
                "com.etoro.openbook",
                sha512("cb25dbe776a642df7c6cb65576a086443e85b6e6ea98e9d37d86fadf5c8ce59408f214c28ed0d3f9da9e03e390063cc67bf15ec7dcac5c433cb726da03cee73f"),
                false,
                "etoro.com"
            ),
            app(
                "com.capitalone.credittracker",
                sha512("ddb5185796ba49bfe5912668e21a1fc292174f899166e1f79730a80012fcd6140acde441f2946781d90d5ac84ace331a3a052af7ea60872ed12de0535b329726"),
                false,
                "capitalone.com"
            ),
            app(
                "fr.fdj.apps.fdj",
                sha512("cb0529da1f85bf9240b959c1c814aebca84d4cb2d0246db8e83450c3949d70ef566716764275a02aa93472f008aa4668ba088d42a65b0178ac2c530e14fe3b2b"),
                false,
                "fdj.fr"
            ),
            app(
                "com.phonevalley.progressive",
                sha512("cf3a371452b662d331d4d9e469ecf8e08b6d3f71dec7d487bd761c7d4c0e1ba96b20d7699517d295581a96352a16625c84e693c151767a1a6ccd0a4cdf1d2fb9"),
                false,
                "progressive.com"
            ),
            app(
                "com.poleemploi.poleemploietmoi",
                sha512("4303bb1b31e8a78c7082a23de15b09fdc946bea63140a13dc2ee3ff1c6c1e25a2c65d9797056b5541fe57eddb68a432932c4875cc0840044ea838f8bd4bbd465"),
                false,
                "pole-emploi.fr"
            ),
            app(
                "nl.robecodirect.android",
                sha512("15a26c6b3dbe1109dd8740cc7122b93c3ebbbb1f84d723a1ac9a8d12cb560fbf7f144367c347b565b24d3873b1b32fe8200ca684db40bf08e4fb50f1c37eb8b5"),
                false,
                "robeco.nl"
            ),
            app(
                "com.wholefoods.wholefoodsmarket",
                sha512("c5712efcbe8b79b2386706bb82358531e437bbcc787481059517d0c63845038f3ef6293389e472d312ad0e697a7553bcb14c55d76eed5f1bc057c3306a3f4d75"),
                false,
                "amazon.com"
            ),
            app(
                "com.caremark.caremark",
                sha512("539282e276ef03ce595a9710123041ae25c56c0ce02123306c845484ef59ba0caaa3b8a2ec3871ae1785170e98713a5f453fa48a7e4611e4e66441697dcc42b8"),
                false,
                "caremark.com"
            ),
            app(
                "com.ent.mobile",
                sha512("68d6a2dfaf55514e740be09fe2535e8d5b4eee8eabb1972bcd3a471726c8b3debd27faeab4472787c00c7ff5a1ba4b363ef983b5618941bb517fbcbd301a9d35"),
                false,
                "ent.com"
            ),
            app(
                "ca.capitalone.enterprisemobilebanking",
                sha512("ddb5185796ba49bfe5912668e21a1fc292174f899166e1f79730a80012fcd6140acde441f2946781d90d5ac84ace331a3a052af7ea60872ed12de0535b329726"),
                false,
                "capitalone.com"
            ),
            app(
                "com.microsoft.windowsintune.companyportal",
                sha512("8cfa4ca1a36f73148b317e321b80b719ff3aaed4ea877dd2aa9b912a0e1638ff8c9e7a40e76cd982f28b5bbe94e02aaa7faf2268193d5bb93f8e172289202d81"),
                false,
                "microsoftonline.com"
            ),
            app(
                "com.tastyworks.app",
                sha512("1db73978b1c0cc4a410f5740982e85caa8572dc02b4e54d7fc338d6e16ced4a6b16b0d1c03b40d42fa837874e98a35da7d6625b48f511791d554bd082be5a2e3"),
                false,
                "tastyworks.com"
            ),
            app(
                "com.ticketmaster.mobile.android.na",
                sha512("687ea9c99159430168076c37235bba3658a31868d18bbc23b5025f469d0e2e7adae08f9c53cf171f24b314cb34194363100e630445a51ece2feafae67029e97d"),
                false,
                "ticketmaster.com"
            ),
            app(
                "com.paypal.merchant.client",
                sha512("ac5d8132d0d7e8de6eca6c707f64b2be9afb916d604d41e101357217c761ae6444f26be6590d4ac123af7310049e9339da42ff56b4b7a261f792af88e8de9245"),
                false,
                "paypal.com"
            ),
            app(
                "com.orange.mylivebox.fr",
                sha512("3b966a10dc81aadd50e1536640d84071706adabfb36cae8ae7c07f0aa50bd0e49fc976fcb5e6ff2887b5119b9d3614ee8e10796bc4e0a8bd8113d866433ab8b6"),
                false,
                "orange.fr"
            ),
            app(
                "com.orange.orangeetmoi",
                sha512("3b966a10dc81aadd50e1536640d84071706adabfb36cae8ae7c07f0aa50bd0e49fc976fcb5e6ff2887b5119b9d3614ee8e10796bc4e0a8bd8113d866433ab8b6"),
                false,
                "orange.fr"
            ),
            app(
                "de.consorsbank",
                sha512("029c5857d314abe3793c9abfdeee529b33b7ba2dfc0f67cd283e13142441716f458536d5b148c4ad1b40dd2013e4603e412ca9598e21a19bfb87f82ed841496a"),
                false,
                "consorsbank.de"
            ),
            app(
                "com.cineworld.app",
                sha512("767c03a58b8e85540246e2fb6db566a38523e1c0d05e5dad1bccd9a2a175cd08bc55bb4a9f86139d98d322e93d4045612c7083d24c115da234962bf3b9f54f63"),
                false,
                "cineworld.co.uk"
            ),
            app(
                "de.payback.client.android",
                sha512("4042977bc4b6fd3a80749c5536452f267c3f7c6f53605aa6d5bb097d619f18a16d6863d8f80025d7266fbfaad93e01dbd3988b34810c80fc3fec414966557b4d"),
                false,
                "payback.de"
            ),
            app(
                "com.reddit.frontpage",
                sha512("650e7962ac47408a29326926493a8cf5a128f937bfddca0cc56185a28607f1e1086f3376d32e7d7e1edb9eb0a23329e6300bc36a7599a854f0a2e245db203536"),
                false,
                "reddit.com"
            ),
            app(
                "ca.pcfinancial.bank",
                sha512("243cdf4a517b4beffe50c2919b02245d878f653890d84103996f74c32ebec5a71eb956cecec9bb4063bf028016e96b9dd31b0787a79d9242d334322030bb739b"),
                false,
                "pcfinancial.ca"
            ),
            app(
                "com.golden1.mobilebanking",
                sha512("a53db59c3227f75083c62d9d999edeae10935e8620c5ae390ddf0b8786a5340dd829f62b9a16fb1b9547cff8bd5691c7acbb9fe374a084950e3abd458b19313e"),
                false,
                "golden1.com"
            ),
            app(
                "com.ba.mobile",
                sha512("a9f218caadecba94d5fe9c7d428cf1398a7c9f1ad471133f33c8419d2f1755e3c05c0ed1878875876a4e0324b2e16550fc5a22e9077cd115655a3d260e86d721"),
                false,
                "britishairways.com"
            ),
            app(
                "com.blizzard.messenger",
                sha512("6a03c2a77de3e8adbcccf0fd0d12de8504cb139d986108d341a7f67680c14a1b588386d48b8f31d9fe7921d4b0e87e012fc3fd3794a61a712303a79745f2a535"),
                false,
                "battle.net"
            ),
            app(
                "com.facebook.mlite",
                sha512("cd0c5bea15efd4c2620b5632a2d7618bc1cffb2edfc0f70e2f03ce593c162a93f655771bb2e222238889d4a5740f3dcbcd5b14b8a266602048500c67b0f07d14"),
                false,
                "facebook.com"
            ),
            app(
                "com.pheaa.mobile.pheaafls",
                sha512("454e08e218eb312051c070ea45ced60b95e57058deff3a07be7a67152de9aa87e638ea4987cbcc6fb6cc32b1c11aa5e24eca9c361c7b080e40ddda1f4a6b18cf"),
                false,
                "myfedloan.org"
            ),
            app(
                "de.tutao.tutanota",
                sha512("1c61538b609c3596d91f77adf86708f23bdaa2691c1e391a0cd100be432386ecbaa8aa78642ee456aace54cbb9aed1eafa79074193bf289e79b7064b2d976348"),
                false,
                "tutanota.com"
            ),
            app(
                "com.zoosk.zoosk",
                sha512("411fa3e068ae7608d734c7b875b1adbb479b3a435cfa69b41b08d6784bdfe65f63e2ee2f185300af277f6d9137c1f31d4c92f3aaddd2b2205d4c9a072cc4e8d6"),
                false,
                "zoosk.com"
            ),
            app(
                "org.sutterhealth.myhealthonline",
                sha512("58f51db0380f31acae354ef6ace68a0f85d7f7762d566e8d3e808358a9edca8531f3ca850e0787087e38798abd993fe38657966df7ac99c6752f0c09986755a6"),
                false,
                "sutterhealth.org"
            ),
            app(
                "com.symantec.mobilesecurity",
                sha512("39c8af91402f6bcb9ae97c4cc711d937ffa1b69fff841249e10eb8044dec6c46cba7e707ed498c7b6beca28c1499d81b425d92c0aaf5c5fe1208b5fe0fed4abb"),
                false,
                "norton.com"
            ),
            app(
                "com.td",
                sha512("f761b99460f0090606937a3f177a254dd399486d0024b5f99cbbe9d8708d64689fa9a09c3784cd8ef60c42e35f529c7cee6bf06242410a2f7391e5711e785015"),
                false,
                "td.com"
            ),
            app(
                "air.com.vudu.air.DownloaderTablet",
                sha512("e9f6e0d21a5b11b8a827ee32e0cb09f5002d4295d66b142833dd1c724394ee515c10691d2a7c7fdb6711e20e9d44a551cb86e7bbefd6886656f663646c4b4b1a"),
                false,
                "vudu.com"
            ),
            app(
                "uk.co.hsbc.hsbcukmobilebanking",
                sha512("9f501fde9ba91b9a946de2ad278f220340826907507cdd514447e9b46494daeef3f88d2849524c3d196e89accb7d250e0a75b2e54763d749d9f88634a61c7315"),
                false,
                "hsbc.co.uk"
            ),
            app(
                "com.umpquabank.mobilebanking",
                sha512("994ca06ab1624f70720a6715ec649beedb6aa30b64ec1e9dbd331ab0480588c9dbc8c162d733ef410391f66f7cdd33381803fe08c9140f62192049ea9268dceb"),
                false,
                "umpquabank.com"
            ),
            app(
                "com.amhfcu.rda",
                sha512("1b69843b2d471581d7b3ef90e8d9ff638bfeaef715b932e81c3ae409b8307ff5b99fd72ed3c3a983db522b7c4ee0e00524e23f2e61a6cea982e75631cc298126"),
                false,
                "amhfcu.org"
            ),
            app(
                "com.ideomobile.hapoalim",
                sha512("d4978420c38372ba0470a5bad775492fb8278a89a8f0cc795f982b7c7220803069027b50bb49e2f393d486884a2666177f04103403b9bfb149413438de8947f3"),
                false,
                "bankhapoalim.co.il"
            ),
            app(
                "com.ubnt.easyunifi",
                sha512("bfddd30552ed067d90ca87ac00fbb5befc08288e46ee0117ed8b8c28b041613f4c32bc02b90637c7b4bdc05f791bab463a3f542ff88b18d7a656a49a6d1bc2a9"),
                false,
                "ubnt.com"
            ),
            app(
                "com.blizzard.wtcg.hearthstone",
                sha512("6a03c2a77de3e8adbcccf0fd0d12de8504cb139d986108d341a7f67680c14a1b588386d48b8f31d9fe7921d4b0e87e012fc3fd3794a61a712303a79745f2a535"),
                false,
                "battle.net"
            ),
            app(
                "com.alaskaairlines.android",
                sha512("d2c8bb7405f4beddfc28a05b6ee77ad87daf58d5a6adfb60dc7ff3d7ef16f7cb49c78193b5c7827de79409b4b0d27b7a892555a24c2553b315ebb705f4fcc59a"),
                false,
                "alaskaair.com"
            ),
            app(
                "com.usps.myusps_mobile_app_android",
                sha512("e74e14c8a5d1ad99f68462476bfc2fef92982a538928e284b02f251271723cfa2b5d97a0c8843cade80faf721c7bda537baf4690ef4eb22560fce8dd6ac4bfe7"),
                false,
                "usps.com"
            ),
            app(
                "com.canal.android.canal",
                sha512("0fe7a3e83a97611917cb4e2319238a3411c0148178a7e5394860bbaff8b5e832e9f7ea7d49962e1afd11d3309021b29f2a7b74fc2e45c225320311a8708e8d91"),
                false,
                "canal-plus.com"
            ),
            app(
                "com.amc",
                sha512("cf7082bed9b332d29d046ac6ff753692ab2216764637c704926d442e8bfe93fdf93b7b831c3e30c98ef1a92b1246d0b198638ad2c21ab224c604e02960752f28"),
                false,
                "amctheatres.com"
            ),
            app(
                "com.microsoft.xcloud",
                sha512("e6b6ce0c0e06564d5a576263580f471e4a3617399d6636453100bb1590241c390c57bfcbfded8ee0b20eac90debf951c1908f2641c1db780d8708daec85701b0"),
                false,
                "live.com"
            ),
            app(
                "com.facebook.pages.app",
                sha512("cd0c5bea15efd4c2620b5632a2d7618bc1cffb2edfc0f70e2f03ce593c162a93f655771bb2e222238889d4a5740f3dcbcd5b14b8a266602048500c67b0f07d14"),
                false,
                "facebook.com"
            ),
            app(
                "org.alliant.mobile",
                sha512("bfaee12dbb19ebe1a6a3b843c0e865ccea7f060684f79f63e3de54b2119cb6f336fec89d96199e30d399e1b1c8d31e522a55a96805da5228b76e6859af35595f"),
                false,
                "alliantcreditunion.com"
            ),
            app(
                "com.bskyb.skyservice",
                sha512("14943d1b8e4bf9455da349d48eb7796b2df4c04be22fc9acc789a103471de49579c74edf5729ebaea1850b0626e1634869a99107dfce78b3f22a088269362e50"),
                false,
                "sky.com"
            ),
            app(
                "com.yahoo.mobile.client.android.fantasyfootball",
                sha512("ed2bc72f3baae2046698d979317593c3da9498f0a274d17db95cec07a02e28dfcadb5c2c34f799027914498bdbf60ca30be9c6375b20a545ba71d29ff0f81b5d"),
                false,
                "yahoo.com"
            ),
            app(
                "it.hype.app",
                sha512("c2d2966c4c596b3429958529a30d2463b73fcebc2dd548926db1ffbf518a87afebc7b8c8d2371075aeb1a47356870be644e2274ff2a466d4d92c6b83fec244e4"),
                false,
                "hype.it"
            ),
            app(
                "com.fidelity.wi.activity",
                sha512("78fce2d0989aff461fb96c9a50472e0ec602dc68c59a8188e9e84de7649e9f3a0bb2e61d01e786490ab92511162d5382baa67c913eba7e3ccdb161eb44ed5f4b"),
                false,
                "fidelity.com"
            ),
            app(
                "com.directv.dvrscheduler",
                sha512("2181cc562ce0db3948634c78f6e78a10fc5d37add158c7e9f661d933606de5d1e73508466ef920ab41fca8629f5aa649c077c6701229635aa3b78199627538ec"),
                false,
                "directv.com"
            ),
            app(
                "com.brighthouse.mybhn",
                sha512("7a76e47ee8efea5ff93e8e23d63448c480e2c3553f5f0376a6db4417f92b4a269fb586021895d97b496739e9683983202c2896a93f54f2061529ad58460c415b"),
                false,
                "spectrum.net"
            ),
            app(
                "com.orange.mysosh",
                sha512("3b966a10dc81aadd50e1536640d84071706adabfb36cae8ae7c07f0aa50bd0e49fc976fcb5e6ff2887b5119b9d3614ee8e10796bc4e0a8bd8113d866433ab8b6"),
                false,
                "orange.fr"
            ),
            app(
                "com.tesco.clubcardmobile",
                sha512("fc0bd6945f17b85334e367ee97f22ed8dac0864e70fec11f46763de482b0216a5d841a866e1393ce44c40a22f9b16d403b160f19f8665efe3a380c6c7e584f4a"),
                false,
                "tesco.com"
            ),
            app(
                "com.pcfinancial.mobile",
                sha512("31eb134ffe5af2e3b529ea0c671b59c3d5947f2f044f9ece3e1576cebe14e116aad91900349bacfb1bc0666039d58b2b63473d84983c52bd73b29f5bf5afa951"),
                false,
                "simplii.com"
            ),
            app(
                "com.bbc.sounds",
                sha512("9d80779121e973c6ac955333dee187c6286a912d78c595bb151c348f01b2cff76b1cdc26e6c71764ffe6794fb2d8561518417c7339ace7a623d0fd5bc9723c8d"),
                false,
                "bbc.com"
            ),
            app(
                "com.microsoft.office.word",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "live.com"
            ),
            app(
                "com.att.thanks",
                sha512("4d2024a94e8de8343715f68cb9102d4a2ec7df27ab846251c491143e06d13749c978e46acb8bfc5693a8ba2a6b31d3dfad0725ec3adee7aea83a04bc232ae46d"),
                false,
                "att.com"
            ),
            app(
                "us.zoom.videomeetings",
                sha512("38467580ec4eef5374ea66e3284aae7aea1f05f715b242df3404c163ba7545fa06a2a06d633b010900fbee213b3fc7919e8de221e103ed677ca981ecd9b9a29b"),
                false,
                "zoom.us"
            ),
            app(
                "com.tradestation.MobileTrading",
                sha512("f5bde90b405bb7afa34fa40a7f3a7a85d7149124da4a4c8b06d6f7605752534f713915a2d285b0ef45ec425832185cafb1cd8eca3031731a8492c8a2f450709b"),
                false,
                "tradestation.com"
            ),
            app(
                "com.privateinternetaccess.android",
                sha512("715ffcf92474a7fdc232cb618a942d4e1e7690f3936f446ce47dbc56133181ceb650ed3bdf1cbf42e6e9e0c2df4b6dfaa4def86f5fa704c5f8455a4044804fec"),
                false,
                "privateinternetaccess.com"
            ),
            app(
                "com.activision.callofduty.companion",
                sha512("7c30abfe5c084287a9f503fcf748c0d4143e73523092e40b9d256d3d53e805811ad85d1678d7a3369e5a8c4d7823f8bb73b0ad20e9800f4b3c6363014882118b"),
                false,
                "sonyentertainmentnetwork.com"
            ),
            app(
                "com.boursorama.android.clients",
                sha512("ac3f2fc716d5a8eb4d24735fe5064ae686c9e705b99ce6890082d8e65d122386bf4ad24bdec81d42a850b1a14ad74529496d924396200e46be75db623546b875"),
                false,
                "boursorama.com"
            ),
            app(
                "com.acorns.android",
                sha512("969b9c7828867107bf5edf6a341dc68fde705bf3cf13e755300940b3579666fa8b426faba89c742f0288ab95a0f28f0d60e5452ec569c51e1aa64e6e0d73315a"),
                false,
                "acorns.com"
            ),
            app(
                "com.blizzard.bma",
                sha512("6a03c2a77de3e8adbcccf0fd0d12de8504cb139d986108d341a7f67680c14a1b588386d48b8f31d9fe7921d4b0e87e012fc3fd3794a61a712303a79745f2a535"),
                false,
                "battle.net"
            ),
            app(
                "cdiscount.mobile",
                sha512("68f58d8f19d0be88c5f311496c2ac2606c2b58fe15d0f86de2b528d70760d8dda7e3b9506b01e4ac8a9b097cd13bb3a72e46323819891d9b66ab121e46e7c70e"),
                false,
                "cdiscount.com"
            ),
            app(
                "mega.privacy.android.app",
                sha512("a4198cfe73bad3cf77bb85fb0fe09a9d3efa4a899d47310c13ce93241f4119899227d7c3cae679440c3e0e8d73e0e8f0e53fee57ee5f538aaf7069f2ada20edb"),
                false,
                "mega.nz"
            ),
            app(
                "com.firstbank.mobilebanking",
                sha512("6704c81d079da169add88e55e7a3ca74636f6b15f443b9f66971418a1901cceef403078c25befbabf946de45e8c608bf4b4247b889247b38cac0c45974c94517"),
                false,
                "efirstbank.com"
            ),
            app(
                "org.vystarcu.mobilebanking",
                sha512("50b9b581a0d1268c987a182f943ffaca90803338e66082219b7ebae1ee08778a952af1fabc7f099203c6f641e2cac3e106fb69081c45961d6d5dc99f8c5cdfbb"),
                false,
                "vystarcu.org"
            ),
            app(
                "com.ms.office365admin",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "microsoftonline.com"
            ),
            app(
                "com.forex.whitelabel.forex",
                sha512("0e189ecf2a34328d456fe7147dc83b0c50ab5185ee9b5305124e9e9ea061b36b3f511f07dcf3816779c97b4174dacb3480f28b6820bc88c61fd3454f4ff7b9f2"),
                false,
                "forex.com"
            ),
            app(
                "com.disney.wdw.android",
                sha512("c0cc05eb0b8b4fb8ef0fc14b3235651991a3bc56396a4ca7d3359d4553c9d82c6746c01275795822911dd6b501911d9fb0258e863e1040c5d77caab1eb19c52e"),
                false,
                "go.com"
            ),
            app(
                "com.ikea.kompis",
                sha512("d7d1c2f13cd20212610d20d2b123f1794484a3ab71c4d438a91d0d7a525b4d8f65fec2eb7fb8ffcfce707295550e10485b4923e25e9d3fed91f02dbfd0a481b1"),
                false,
                "ikea.com"
            ),
            app(
                "com.wealthfront",
                sha512("b4730db4a47c6e61a6b9f98f20aec54d0e455389fa642df96a247a34b71cd00f8397c13219ecb0e89643104d1ebd8243b32201729165ffb70d008785de2919bb"),
                false,
                "wealthfront.com"
            ),
            app(
                "com.ccubank.mobile",
                sha512("e0dda9dd13f62f4227ecbb4eda158fdc5b2d74b213eeb63e5d5c26b9273ab3b3b54ff2278f86bb3a969c6be74fcd7cf8ba3ef546f927a43b4778727b3ad3380d"),
                false,
                "myconsumers.org"
            ),
            app(
                "uk.co.theofficialnationallotteryapp.android.play",
                sha512("43bef0a40fde52cea51221ca3a5578ada911008ba58429152a025b0f65ae8a3b8c7abe8b178fe1a85b179fc0b86b1a66f7c10c7f51c6bdb6f93264c59cc660da"),
                false,
                "national-lottery.co.uk"
            ),
            app(
                "com.microsoft.launcher",
                sha512("5a151d874e199102e635bfff94a9a8872a8374f3165c7708d0edc0be82ccb6017fb269c40f8afe560baf823e9de101bbec99776af50ab7a2dea85d9324f655ce"),
                false,
                "live.com"
            ),
            app(
                "com.wix.android",
                sha512("b71f223de58f23ec4896d616f06c693d53203e63da42df5be4892dc96e1f6677a13dfd0205244882a7b334c483f018d0346e270e5a40289518c51c8a4da18f1d"),
                false,
                "wix.com"
            ),
            app(
                "com.aaa.android.discounts",
                sha512("3af55548ef63ef1b179fd256d1bdcdc69a76dd55f6fa39e5050a0847f52bc2d4b07715e6988cc51733d37b1eab3b79edefe4cb2c952f4929dabee75d9e300d44"),
                false,
                "aaa.com"
            ),
            app(
                "com.starfinanz.smob.android.sfinanzstatus",
                sha512("d08963938ec98b05d604b4d3ab295e2150eb23cfa55edc32bef70e274809877ea4371ee7682d689445d096b900be46dcb0c0ccb3c6f12793fe988c952444855d"),
                false,
                "sparkasse.de"
            ),
            app(
                "com.carrefour.fid.android",
                sha512("57d6165a20e78381268655341f1fc85e95e46746f199b7a369b52c0fc1ae08e241f1b635cc6375b928ad0313aefea64f570688a35db7c687aaf36803434821a0"),
                false,
                "carrefour.fr"
            ),
            app(
                "com.commercebank.mobile",
                sha512("2d1455b08fa047be32ba2217047ab6dc0d2aebdc07f1e0c613136f97b5dd1ee3d6c06ff6dd60616b8e7b896807c8a9125fdb1cb70e1821945362aa80410fa85c"),
                false,
                "commercebank.com"
            ),
            app(
                "com.cardinalcommerce.greendot",
                sha512("ea695342cf0cfbec945b4bcb765f71e5a8f61ea53f331228efa1ba92c01c67bf0262ca84551680ea205a4a90d110fab32997377c28efc129d9693864553caa33"),
                false,
                "greendot.com"
            ),
            app(
                "com.comerica.mobilebanking",
                sha512("aa2ac0d5201d43dde3d5670732906fcfb991040030a22ce3b6e9c40737a41a36a15db9b10572fbee08efff2060fd0d94ffcf5f2f301ee0891e5c928326debfae"),
                false,
                "comerica.com"
            ),
            app(
                "com.tradingview.tradingviewapp",
                sha512("1643234949bd1ac0b279c65de155970b22853ec5e61602f379385142bf92dd1c3a2e6d92118c8124ac0a02088382a385abffc1465f1ccbff8bf450bf8105c9b4"),
                false,
                "tradingview.com"
            ),
            app(
                "com.transferwise.android",
                sha512("f067d70337a58c4afdf6176cad8aed64b97a9c3722484b3dcd697537a6e4424ce374e554c88b2c68a331fd0c1d24c589c74bb52643b6c7f9863265c12c67e2fd"),
                false,
                "transferwise.com"
            ),
            app(
                "com.microsoft.todos",
                sha512("55aaeb877a2ea72d1bb43bc32d5c1c5c60698ac9ccd942a906b9c7ba1398d93aff41e31c2f6a84f2383bda9113d99a440518993e7bda10bc79b6ba2d4b2e7ce5"),
                false,
                "live.com"
            ),
            app(
                "com.lowes.android",
                sha512("7de1f9bc96149a8761f9f03c4d2ef8c9a809708c9c2a0c7c5ad4077f55de8caa308bf13041b6558eb3d8c31ed1dc121d6aa309cc18ff75afeb7c63b028c815bf"),
                false,
                "lowes.com"
            ),
            app(
                "com.papajohns.android",
                sha512("86f8d00b4929ea1dd2d4c648cd61ac96cdcd73f49dce4d3b1f6bc2f5b9b0996d9d22d5744e07154746250b4e68f28540f81c16a3b314a3cdfbe24fdd4a3cf60a"),
                false,
                "papajohns.com"
            ),
            app(
                "fr.bred.fr",
                sha512("c5513fb5cd66212bfd27b2534595cb1bbdaf8d46fb35dcbf2975afb43408ac73b0e8cc638a2045489cce860fb389d9c125ec6a310e19732de72365561d240fc6"),
                false,
                "bred.fr"
            ),
            app(
                "com.bitdefender.centralmgmt",
                sha512("d36cc33c5ed5c451ed373d0e20c84ee4245e0fc8a0ae3f02df052312f5a0bce3c545524cc27a68d1b4f8ef169f549b272a0f09956b22c61d2c52de7eae446b48"),
                false,
                "bitdefender.com"
            ),
            app(
                "com.mobile.tiaa.cref",
                sha512("e596160e5079d1b9e94e7064e23bc8aa3ea152fb46c02ff6955741c9c1ae6cf3471c0a23836a045973aa39b125d460134f0c5f823d26940c9827e63f9ec0db1f"),
                false,
                "tiaa.org"
            ),
            app(
                "com.grubhub.android",
                sha512("7f0d1c47d2f9c2b84c2fcea6083e01436d2d14222de8d138a752602fb37b88732a57b77dd1e51b029de22c5b8e7041c490183b6e8fd2840c8206132b979c1c97"),
                false,
                "grubhub.com"
            ),
            app(
                "com.tivophone.android",
                sha512("b9d740ecafffc7963c106c316463909b4a7d4104fa467686dc3e1dd5b5be41c39029482061348b8280d58402369ec3a1eab3d3f13f8d9773347e8df8acdab3b1"),
                false,
                "tivo.com"
            ),
            app(
                "com.rubenmayayo.reddit",
                sha512("63a2d60fe210e6dfbf40f0bfb0178bc194f4c4e70de4954a2b2710e934f70fe80f08a1e710dfc46180a66393f4525da58a913df55b0f2d28966863d7cdc19c70"),
                false,
                "reddit.com"
            ),
            app(
                "com.adobe.psmobile",
                sha512("e49f93f9ecdce989953d2522f6044514a86ec9120bcedef44fd9b996fc4c5e8f3c0e86acd10018f344c818d5a1d39e33741accfe1d8e5e724525a62bb32b951b"),
                false,
                "adobe.com"
            ),
            app(
                "com.blade.shadowcloudgaming",
                sha512("9559c067efcd0120b52f73bcf4d228a296486cb163d1520980525173907cd8c62abf39f44860ecf14edf9bf6d0ba87e8442cff58cfdcadbe71d844f395037513"),
                false,
                "shadow.tech"
            ),
            app(
                "com.sec.android.app.shealth",
                sha512("d2277c7f8731d24e7ba9f895e0b7b43086f17102f648fcfca20947149d6e7e9d01f15f657503e2b47208a648cab18445969fc75c7a6032ac639bd3d196522c04"),
                false,
                "samsung.com"
            ),
            app(
                "com.authy.authy",
                sha512("b0c929d0c3af1f9dbc1b8b1d7786e7ec21cf8a7560e53a455085b4ff1c6bb9b2b33ca893db74d677c3122855cd4d1da68e9098c7ec262b6062320e30fa3928ba"),
                false,
                "authy.com"
            ),
            app(
                "com.yum.pizzahut",
                sha512("036c63affc55281b06780f6ae38947aef092908f9a9bb91c87bf3b90654c81f39fdf1ea6b3f5b255b3eb938363de702f4d9a8183fcba729400f1d88f560ce071"),
                false,
                "pizzahut.com"
            ),
            app(
                "com.americanexpress.android.acctsvcs.ca",
                sha512("bd0e86334e5d465c7a0dafc0cbcb217c739f37c6b5cdabcd51993f9d03c8b63f8b9985ffb6288a7711b1fc934e6167c763908c0a8efd3cc3e182892cc821bdc9"),
                false,
                "americanexpress.com"
            ),
            app(
                "org.mygreatlakes.borrower.mobile",
                sha512("18ecec995e16dabeda34f57f34804e669591e5f5533bccc5148f2c0e56c99ede528de092d92585a05338ff79d4a8e5198e57e9d213dcb139fbee28e7042bbf67"),
                false,
                "mygreatlakes.org"
            ),
            app(
                "com.americanexpress.android.acctsvcs.au",
                sha512("bd0e86334e5d465c7a0dafc0cbcb217c739f37c6b5cdabcd51993f9d03c8b63f8b9985ffb6288a7711b1fc934e6167c763908c0a8efd3cc3e182892cc821bdc9"),
                false,
                "americanexpress.com"
            ),
            app(
                "com.laposte.bnum.digiposteplus",
                sha512("b1ed1c80a943a10a3939523825e05310ee8d2c5592811814ba88de117b0b82865f1310f70737954dbbf8e702960481021a07402dd07f7610fd327746bad859c6"),
                false,
                "laposte.fr"
            ),
            app(
                "de.gmx.mobile.android.mail",
                sha512("b65de88422bf4c99e3badb98e42b2153a01a5930ea1d48a0060b38771eeb80af2c4146dc5ea0b7baf804a2eb6a3663804509d0f84135d2e64cd45c9fbff9890f"),
                false,
                "gmx.net"
            ),
            app(
                "com.jetblue.JetBlueAndroid",
                sha512("1e00ccdb18ebbf6ac3c733b834c58046034afb247414d777ecb294099c53b24f8e982be1635a10889d3491ca3a90abc37b7677ebd0a99c0b3e66043848555b25"),
                false,
                "jetblue.com"
            ),
            app(
                "au.com.commsec.android.CommSec",
                sha512("9559d060a7182c0a08ca62de79eaea929d735a7187b455cec8bb8f555f5e326c59cf582853a3f34458dc2efd9508fa51cae0f7c106f6238f0de6f27a7a0d6086"),
                false,
                "commsec.com.au"
            ),
            app(
                "com.miteksystems.android.mobiledeposit.brandable.a",
                sha512("281e2e1d7ccbb9bc97cfed227fad15249418771902447a96374adeb7ae9c3e4dd87de3347048171c0b62f58234ddbc5a8965fe2fcac91bf335f4d09c1bd03edb"),
                false,
                "alaskausa.org"
            ),
            app(
                "ca.bnc.android",
                sha512("97e4971cc76a268204094272fc5cf583a6b9102091abc1b05313fdd1b35fe69aec022de2d9b00df399915793f3ba21ea6ca0c6d981e74a6cf20b444e4237d9d4"),
                false,
                "bnc.ca"
            ),
            app(
                "org.westpac.bank",
                sha512("7039ede38f35d40b42acbd74d508f577bf0fe2bc4e6eb359fee9a8207458b81466e6e9425569013319c686a6addd561b7e4c39055b1fd9ed0dd2aa883a835d15"),
                false,
                "westpac.com.au"
            ),
            app(
                "com.paypal.here",
                sha512("ac5d8132d0d7e8de6eca6c707f64b2be9afb916d604d41e101357217c761ae6444f26be6590d4ac123af7310049e9339da42ff56b4b7a261f792af88e8de9245"),
                false,
                "paypal.com"
            ),
            app(
                "com.serve.mobile",
                sha512("a8aa7a21043d02e48098f9a6ffa931b1623d8fb9700c706be6eb53f8aba8a807979b4ec342b4cd7ce78450ec4549374a0263cdcc3da8e426add671274803e7c1"),
                false,
                "serve.com"
            ),
            app(
                "com.callcredit.noddle",
                sha512("8a92c0dece2f30ea0d1e36ab02dd98d44437f4f00db867ea42d0a4786b21df9e58f79c5f5b26089baa10eb01537ba5c244092f6c57358b7f076982dd7d542e49"),
                false,
                "creditkarma.co.uk"
            ),
            app(
                "com.subaru.telematics.app.remote",
                sha512("68e8758fd0243e5a7ed5a6ecde0049c05eae72b5b08601f764bf3d3946517fa2da7ac2e2ffea8549eb04133a7e8388cfd8cda96d8d42e2e1ee092f26d2257581"),
                false,
                "mysubaru.com"
            ),
            app(
                "com.nusendacreditunion5093.mobile",
                sha512("b214fda3240ad812cd3ad9e5ba1149922a4d9895eb9efe859ca883bac9bad2ea28133d2f666603701d7cf909eed2ad662cbb02d9007049d7c94bedecf3707a05"),
                false,
                "nusenda.org"
            ),
            app(
                "com.key.android",
                sha512("c98326764553929d47723648c23d5ab07aecf7259083bbf782201aad8e4b5478f58508b554bc9ffcaec03ffb9d75750df60b094fc0d95d08255c2e4e81f84d13"),
                false,
                "key.com"
            ),
            app(
                "com.citizensbank.androidapp",
                sha512("cd97be4b9839eb99632190dc832a920262cd5bdaee1764ca0c86e52c76272e2208ed6d83e1d82e30bba4c32d9e37597ab20068864392211ee3ae0f4fbe8e6421"),
                false,
                "citizensbankonline.com"
            ),
            app(
                "com.m1finance.android",
                sha512("bb58313dbe4e07fdbe34f3399f1ca3c06de907cd2c3d5e2b57ae6d488c7fd230a429a7619ff350b47243f51814cd87d272ad3bc5065e145abb2e350d70c97691"),
                false,
                "m1finance.com"
            ),
            app(
                "com.ellation.vrv",
                sha512("0112c1db045a3e16a550191ab5f6d77287e039770fb7d595d65259c62dec9f06832dc7d802f27a0aa2abcf77f75e1def4f9f3a8dfdf2c1a8d00655072722b5fe"),
                false,
                "vrv.co"
            ),
            app(
                "com.humana.myhumana",
                sha512("e3c2b5c0a4d0f4110d50a1a77d6dd5f1671ff75563b40c7ae0bc7a8ec657324de0bfdc9d8ef5b96920f732fdecc34c4adb29e53a6345bde3b657fb0d742b3513"),
                false,
                "humana.com"
            ),
            app(
                "com.intuit.turbo",
                sha512("b22e0f94b5e0e86b194df35652c83d18ab9b455b413dbd112712296bddf79e3358011ce009798cacec0771d1434f206a45cde2ede09877ed98b5f339f52a5bde"),
                false,
                "intuit.com"
            ),
            app(
                "com.ford.fordpass",
                sha512("14cf9f7478af93c963910d7d1ee84df349d90293d1180062fa0de1c53116f762c4f7584d939b390b52f8c2659beb19d8e0429e0dd0c45b6aac1bda44f2612a5c"),
                false,
                "ford.com"
            ),
            app(
                "com.syf.mysynchrony",
                sha512("d61299cb3928449a6badae3e1a37eaaa99241b3219cd0c6de0339d131308f15e8045892547397b576a9013d04eca30aad4732cd779bd4e75008e459eef3939b5"),
                false,
                "mysynchrony.com"
            ),
            app(
                "com.cigna.mobile.mycigna",
                sha512("a6398c93acab9b4272ae2463447b0e3f4306d52333981310858149ce552e32739830e2fe5be06b626d7270c981a666957d5ef4ff235e310c3e3c3df74a201571"),
                false,
                "cigna.com"
            ),
            app(
                "au.com.bankwest.mobile",
                sha512("2f7cf27f7f92586dab95bd176e14d85e3ff395c649b5e297160822b8c76bd3223e1e445036274a1f6d77dbe05b2d7b17df619ccd6a8122e2f10a1dc66f740e69"),
                false,
                "bankwest.com.au"
            ),
            app(
                "com.ing.rs.ingretire",
                sha512("3cddbcc7c493161907e52add4b0a35829d075bd3c47cc59e23a7064317dcf50efacbd0f4215786ef22cffcfe4ecc856e70108f7821512eff083c008d30a8ea4f"),
                false,
                "voya.com"
            ),
            app(
                "org.coastalfcu.mobile",
                sha512("ecde1045abb6b3f7cbd16448c296606c26749a84d3337da953888097ba69a4086412929b195e0241fa0181d7329409e5e5dab3ff80aed8a127578ae5c583275c"),
                false,
                "coastal24.com"
            ),
            app(
                "com.deviantart.android.damobile",
                sha512("755f4420352cbdf7360240c42a807e3ce3a17c1f53355a742776b5dac5ef5bf7399a4fe6628cd7c58d7b561e94d39925996413de440ee5e7a906b1c3c10d2e63"),
                false,
                "deviantart.com"
            ),
            app(
                "com.paylocity.paylocitymobile",
                sha512("bd84cfde3fd8faf8cf09f3faabb1a45f61f1c0070e5457a19de576e29474eeef3eec8fe39b1415496dc484e594dc5777bf836884033ac2cc0b17aa558ef1e0cd"),
                false,
                "paylocity.com"
            ),
            app(
                "com.qtrade.app",
                sha512("881bbf83c1264e7e70a69eac5bc3d1cf9750369864f4ce971eebecaaf95773432f7ae6f5764555b56cae33a2ddcd437d5e83e77b5b22e6f8d4c7bd7d9c3582dc"),
                false,
                "qtrade.ca"
            ),
            app(
                "com.personalcapital.pcapandroid",
                sha512("71a020645cec5380292b2a225eb0000c19602f4d24d02b34d31327c020ff7995aaebd6062718bda82937bc3d4ab7f71e6d2657b97186692f5cbd3b2b4c6ff9f3"),
                false,
                "personalcapital.com"
            ),
            app(
                "com.amazon.now",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.adt.pulse",
                sha512("70606b237aa91cebd2f086a43540721f8e06cf1c878d76b6340ebc5bc232ca6ac4e8d444bcb5c03305dd9723d5b3ac6858cbe0cfb3a039528f81e0fe35c04b1c"),
                false,
                "adtpulse.com"
            ),
            app(
                "com.softek.ofxclmobile.warrenfcu",
                sha512("2b7e68a82bb063df24980279e94d51ac449f5f2c614ed175234be985a60cc9137120436d6836d25177d2772508852ef9c07094a4ce785a551f9b3a070c065eb6"),
                false,
                "bluefcu.com"
            ),
            app(
                "com.microsoft.office.excel",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "live.com"
            ),
            app(
                "com.medco.medcopharmacy",
                sha512("828c2761f2b04e6cc101ecaa36e317d882345517799aabbb27600a664a434efa9ba606caa277db604cbc0a37f0433c724882fe824c3cebdafa4c41ca794ced2e"),
                false,
                "express-scripts.com"
            ),
            app(
                "com.citibank.mobile.au",
                sha512("37729c44e2de3c20f7ab94c421a33c16291cb31c0865199b08573cb93a5fba77445035cb0a40371fb3f01c8d4ac59ccbc6adda71044de9e213def5b1445d33d0"),
                false,
                "citibank.com.au"
            ),
            app(
                "ch.viseca.visecaone",
                sha512("ef1687973af74c00b194a2748eaf9c1c8a530cef9a52eac493e05812e74166dcf523ce226a26fe75408031002c6a854070e1b4265effb18279f5cf6f59e83b65"),
                false,
                "viseca.ch"
            ),
            app(
                "com.att.shm",
                sha512("4d2024a94e8de8343715f68cb9102d4a2ec7df27ab846251c491143e06d13749c978e46acb8bfc5693a8ba2a6b31d3dfad0725ec3adee7aea83a04bc232ae46d"),
                false,
                "att.com"
            ),
            app(
                "com.hsn.android",
                sha512("4acef2d9e0c67735add1c8e297283dcdc236424bf0f61b56ac5105c694aa5fb9d0547c512529e401890af401832acf107bcd26666d7f73987f5180dac4e282d7"),
                false,
                "hsn.com"
            ),
            app(
                "com.ameriprise.AmeripriseFinancial",
                sha512("d8f828877322833485db548c995f161dd10cf95fcd4f741d42a411ea159f50d53cd7665577914961d0a7ca561751d9bf6ff420d7369501777cffa18245f29c38"),
                false,
                "ameriprise.com"
            ),
            app(
                "com.pnc.ecommerce.mobile.vw.android",
                sha512("19a9161db35a2755ba6b6d4a9839104f218e20b7697cd924dea441c179a67449f5e46ebc69c9de2aa4b8671d3a01c8c45603ccedbcec8bb84cbe63a34fda34d3"),
                false,
                "pnc.com"
            ),
            app(
                "com.netflix.mediaclient",
                sha512("2738f94f6138e476f7dc3fa593e1076550ab6fb6b4eb874489c4f0ad36106065cef7d26a1366044616cc3f5a8ba20c098b2f3b3ac0730b4f46934f7867e53f86"),
                false,
                "netflix.com"
            ),
            app(
                "com.db.pwcc.dbmobile",
                sha512("a2619aefa6683c5878f029bb10cb2adb87ddb65fb02e9fabc419aeb2ca663625e9b271d7c9647c3558517e4cb70cb2044269297d6a9cd19870c8980370015e1c"),
                false,
                "deutsche-bank.de"
            ),
            app(
                "com.adobe.scan.android",
                sha512("ed1ad9e3e977c72263a17e8087ee52912b593078a05c66d285906e51e17a1194dbe2c07e1658a1a6182524dbfd54817cd141c185e0f401162950b6af572a17b2"),
                false,
                "adobe.com"
            ),
            app(
                "com.zwift.android.prod",
                sha512("908eb478cce52e3dd8e9265569ec2acf2f0a5d724ff4849770bbd1d7e92a2138aff6e342b257490c6eefa7fee43144433b5ef88734acf1e3829a767b1a2fe01e"),
                false,
                "zwift.com"
            ),
            app(
                "com.softpauer.f1timingapp2014.basic",
                sha512("592fde62c9e85f649f4fafaaddc60f5f8330c0963d439c5afac307b3f1ad1ea482a3c35c7abeb8e99e076eab8a91b1eed4ea355bdd78c0ae7c27ea825c70eee5"),
                false,
                "formula1.com"
            ),
            app(
                "com.cisco.anyconnect.vpn.android.avf",
                sha512("4f33fd5023cf69102f848c4e439f836c5c5c0cd39a60218418c2e30bb39e6f830e87a1d33a9c02c5f2d308f2e3587f5524dc075009dfaff39728ea7d5f9266cb"),
                false,
                "cisco"
            ),
            app(
                "com.firsttech.firsttech",
                sha512("be6931a43d70e8a6058690d2ac54aa2304c1e53284c00b35537c5baf23ad0d479bb86480e0f98ae7e7a98e33f992881a8f2847533a0f3e22d861589fa339258c"),
                false,
                "firsttechfed.com"
            ),
            app(
                "com.booking",
                sha512("92bb9a670606b22bbbe936c034b5f9d8b2da4220134e0ed0b152445ba733d6f8189d95ff10ecf5f7b640ea0236032009e74a723f94179d80c863e0adf53be127"),
                false,
                "booking.com"
            ),
            app(
                "com.intuit.qbse",
                sha512("76418b69762b49dfc106924b1f725f99eeaec2f3c96f372bec60a83b6b167af5c62478516687424994f1efb3ae9fcb9eec19565de03829df291801d2e228323c"),
                false,
                "intuit.com"
            ),
            app(
                "com.hertz.android.digital",
                sha512("ec5aa41b3c5590d95e745969e7959283794855f31f88373826ca031d13fc1ec47514657112172a2444d930afc84c430d42ad21cae68448e3fc698420d95a2bbc"),
                false,
                "hertz.com"
            ),
            app(
                "com.usbank.mobilebanking",
                sha512("05f57a622433649b2d7062e2ca6b55428abb0c65509de564ac31ce0250f18999d13f51a6c553ae398d2591b6cc2af8018f5a69f5ce5e7fb42c92aa7363b5b641"),
                false,
                "usbank.com"
            ),
            app(
                "com.twentythreeandme.app",
                sha512("a45c4979183b18086022bbb91ffac8a988a60fb89eeea724c213bc1ded9aa355769c80444150003ab17a109d093480c59ca5dd141320598ab790e36d45f8fa46"),
                false,
                "23andme.com"
            ),
            app(
                "com.samsung.android.gearoplugin",
                sha512("0018b67dbb7cbe4ce3ed227c683e63738c491530c59ed76432b6172f78adb2fa98d50230f9d668cdda29e6b80a3718dbad001de679ed006a6087dc3ce0ed5782"),
                false,
                "samsung.com"
            ),
            app(
                "com.ubercab.driver",
                sha512("4a183902a134ca2055a1654c579d56454a0026d5fac509dbad3e506207d051fda717486479156db3981ab3de61a1e3d59052ea904b8663eb4e69507b5d4f6a6e"),
                false,
                "uber.com"
            ),
            app(
                "my.com.maybank2u.m2umobile",
                sha512("09e7457f6da29dc9414941034c4b473c8728887702ab15db1450b159a9118a45153d184c3810ca2549084c1d5ed3641e4d96055612c138cf7c872ded8079d596"),
                false,
                "maybank2u.com.my"
            ),
            app(
                "com.samsung.android.geargplugin",
                sha512("0018b67dbb7cbe4ce3ed227c683e63738c491530c59ed76432b6172f78adb2fa98d50230f9d668cdda29e6b80a3718dbad001de679ed006a6087dc3ce0ed5782"),
                false,
                "samsung.com"
            ),
            app(
                "ca.servus.mbanking",
                sha512("90c551490d3655f1d8ff9e4f41b776eadd6bab4f409ff6c648fa55a0c72d4cd9e9c3b71f66a06706f16e737fbd82d90fac0995a13298856eb74fc63d09973300"),
                false,
                "servus.ca"
            ),
            app(
                "uk.co.bbc.android.sportdomestic",
                sha512("35672b9da2ce845005a086dbee51e88a658ab214a9eab5e95957d3504f0ddf26150e6f49f9a642a669b8296905ddaf2980f86964e636c460dafd212ceb54cc56"),
                false,
                "bbc.com"
            ),
            app(
                "com.tdameritrade.mobile.ac",
                sha512("557eb8c10a9ed87747f702d00e54fa12b0dc29ddcd770b72b43abe36477d560c15ac8f4e8d359764fb4c87801b2acadb7545f12f92c12907cca0bbf0a3fea3c3"),
                false,
                "advisorclient.com"
            ),
            app(
                "com.orange.owtv",
                sha512("3b966a10dc81aadd50e1536640d84071706adabfb36cae8ae7c07f0aa50bd0e49fc976fcb5e6ff2887b5119b9d3614ee8e10796bc4e0a8bd8113d866433ab8b6"),
                false,
                "orange.fr"
            ),
            app(
                "uk.co.patient.patientaccess",
                sha512("374258712f07832da4dde90321853be7026f68e159e00aadf2d9c5876ad003ece332b6399f66decf42d8d4fe6325cb9ab2b3c4f04f70e6c43ec3a734518cb0e2"),
                false,
                "patientaccess.com"
            ),
            app(
                "com.holosfind.showroom",
                sha512("deda417702b3fb22e2b93df591d0f800ea1ed26f3a2b70c34c0e71c0c0663b6d7e21bae138bfc3f458707c895d62cbf0b82783e4821771e8b598791632b15750"),
                false,
                "showroomprive.com"
            ),

            app(
                "com.experian.android",
                sha512("28c9131ba50e5e5e87ddae669a30d19e30b62ff34074cc4c1a5d1e256319814af6d70764617be24c9843e9c7eb83398e0456176b92f66eb560c02a745439af63"),
                false,
                "experian.com"
            ),
            app(
                "com.intuit.turbotax.mobile",
                sha512("76418b69762b49dfc106924b1f725f99eeaec2f3c96f372bec60a83b6b167af5c62478516687424994f1efb3ae9fcb9eec19565de03829df291801d2e228323c"),
                false,
                "intuit.com"
            ),
            app(
                "com.americanexpress.android.acctsvcs.uk",
                sha512("bd0e86334e5d465c7a0dafc0cbcb217c739f37c6b5cdabcd51993f9d03c8b63f8b9985ffb6288a7711b1fc934e6167c763908c0a8efd3cc3e182892cc821bdc9"),
                false,
                "americanexpress.com"
            ),
            app(
                "com.americanexpress.android.acctsvcs.fr",
                sha512("bd0e86334e5d465c7a0dafc0cbcb217c739f37c6b5cdabcd51993f9d03c8b63f8b9985ffb6288a7711b1fc934e6167c763908c0a8efd3cc3e182892cc821bdc9"),
                false,
                "americanexpress.com"
            ),
            app(
                "com.questrade.my",
                sha512("f7972f91a494b3d8be203d9b8b2e0db8a6d23ddc763c586ec2427fbc7694f6c587d5f1bb7ebe9b35b7069ce7074aad7c34b8156221939f7bc2385db5e92d22f2"),
                false,
                "questrade.com"
            ),
            app(
                "com.microsoft.xboxone.smartglass",
                sha512("e6b6ce0c0e06564d5a576263580f471e4a3617399d6636453100bb1590241c390c57bfcbfded8ee0b20eac90debf951c1908f2641c1db780d8708daec85701b0"),
                false,
                "live.com"
            ),
            app(
                "com.sofi.mobile",
                sha512("8bede79ec41080898e7b4097a02e111e59ac3b4a0e4b68233d7bf10ce4f569c00575f9abc7dcc64ddacb850d9c3b01388ba5aa940862c33961124572cb5d86e7"),
                false,
                "sofi.com"
            ),
            app(
                "com.osp.app.signin",
                sha512("0018b67dbb7cbe4ce3ed227c683e63738c491530c59ed76432b6172f78adb2fa98d50230f9d668cdda29e6b80a3718dbad001de679ed006a6087dc3ce0ed5782"),
                false,
                "samsung.com"
            ),
            app(
                "com.alibaba.aliexpresshd",
                sha512("0548b7be925aa6e890aa959051d3bb2d573b85aae86042f29b494aa1a75f29c5df5782e0574575f33006c31d6356a5d0299d9781db7962efea788dc31df3e3d5"),
                false,
                "aliexpress.com"
            ),
            app(
                "com.xiaomi.smarthome",
                sha512("94321f08669df30b3fd44df66288897e7020008fb21e12aba2c02339888e38e8920dba252096aedb447d67975b14ab546af9b704845aefe89d339572b4d3d7d4"),
                false,
                "xiaomi.com"
            ),
            app(
                "com.plexapp.android",
                sha512("43ff818b6a1dcd86c1e8a80db410bd81d7879efad961143bc908895859d07644453f94d8592dfb19dc217b1c0a23a51aa98f2c5b5d397dd710776df29912fb78"),
                false,
                "plex.tv"
            ),
            app(
                "com.jardogs.fmhmobile",
                sha512("4c6428f03c29d2a619d1ac513ec2ee88c9a2616ed824daaa9fe984fcf70edf5d40d41b9e5b42087207b3f13522ffcec5e9399ba64b9edf4d91eeba3531827fca"),
                false,
                "followmyhealth.com"
            ),
            app(
                "com.adobe.reader",
                sha512("ed1ad9e3e977c72263a17e8087ee52912b593078a05c66d285906e51e17a1194dbe2c07e1658a1a6182524dbfd54817cd141c185e0f401162950b6af572a17b2"),
                false,
                "adobe.com"
            ),
            app(
                "fr.bouyguestelecom.ecm.android",
                sha512("e6f5c8291b61c54012f2cb2118410c184a28193e13a448aee7f04f0831934d65b2d7d161ca472c48b6119018ed86d0aac79532ef9bcb36804a62f33ea6e0c28a"),
                false,
                "bouyguestelecom.fr"
            ),
            app(
                "com.infonow.bofa",
                sha512("7d91b78f5d2d1bc023d9841ab6d31791a144e4da458fa6086685a464c88b369251757a08e22b4ef7199f65585e1c9afa5ba8352cd2ac82d1c3ea94c7087a5437"),
                false,
                "bankofamerica.com"
            ),
            app(
                "com.bofa.ecom.marvel",
                sha256("feb11c69864a3ebc39339b8f6225270cd78667ac7884b3e3b50a76375a2c1537"),
                false,
                "bankofamerica.com"
            ),
            app(
                "com.match.android.matchmobile",
                sha512("e49001ec8f1e9f1aa27ad00a58dbbb003da85b10186f6d193503651d5c3ae1c7d04c4c3d24d462beb01f7858fed96c46442d385de8e82a95a00cef18a4e2968e"),
                false,
                "match.com"
            ),
            app(
                "com.healthequity.healthequitymobile",
                sha512("556e008620bfdbb9e726926e348ba47cc85da6b193fe9534e6d42c06d4901459161fc6861b9c077e38a29f0ecdade3a042b187e4d3ab11c5f40f3a9d2cf476ea"),
                false,
                "healthequity.com"
            ),
            app(
                "com.swissquote.android",
                sha512("ad306ee841c7724453bbd18e16ca9ae16e7ffef62e26c4107f697693f3dc0586244e41219889877bfe53bffe70f25c2cbb8ee9c1aa7fc4c3f3c1afe50a5e1145"),
                false,
                "swissquote.ch"
            ),
            app(
                "com.kronos.mobile.android",
                sha512("72a0db52c6d8b7e115cf6ba8b7829d4118f0f05b60878fbb41a8dd46d1717a9aec8befd6b3e7db26d2483e52a7982c62bf214b64e0d51927c2dafa3da5758997"),
                false,
                "kronos.net"
            ),
            app(
                "com.microsoft.office.officehubrow",
                sha512("88f50ba47d29abc9acd50cbb70ecc6b1545037bff35b821b5be50a71a8efb6b4ebccce68e5571a8213440351e8e16abb6b2d1e7eaa8971a9716bc787c559c728"),
                false,
                "live.com"
            ),
            app(
                "com.mobile.uhc",
                sha512("6a68df6bfdbb05d0570b0b25f732c9426b612628b1345b284e801f7cdd9a545216fcc1204ca62b8f59ca63be457fa40a0a65fabd3cec2b54cfe537f760d3ca81"),
                false,
                "myuhc.com"
            ),
            app(
                "com.Nelnet.nelnetmmaapp",
                sha512("43dd6ae4b619c9ad62afac4a7fc7d7e819413aa3431e813817844533032f2dc7c5cc1df3d78d3fb97b6ff9018ba3ef67138b77769858ce88a7341109238beb2e"),
                false,
                "nelnet.com"
            ),
            app(
                "com.amazon.storm.lightning.client.aosp",
                sha512("35ddc40dfb550f4951dcbcf439dabc34c9165b2339093f256a178f90cb73bd2e989156277bf1e7e636834b1add5e4375b38032c009dabf64676ab66f72a54525"),
                false,
                "amazon.com"
            ),
            app(
                "com.anthem.sydney",
                sha512("8e5531c1b0d72a349f08eac7050807a4eb6d0cd8437d69e775403c29cdd56c39eaa8e89b788474e99ee75aec528c056ee436ac515ede42f248a12ceef587f798"),
                false,
                "anthem.com"
            ),
            app(
                "com.ziraat.ziraatmobil",
                sha512("05dc63aee1279533e6a901e64ca6ec709e91bdca071055c5b1b990a06dcc2a875b7096c5efc16c739b029bb73cac1c3fec2cf782b4eeed8d3e9325d59e0063af"),
                false,
                "ziraatbank.com.tr"
            ),
            app(
                "com.primonial.allinpp",
                sha512("65bb86673f98f60dfeb1c8c6d4bba1978c1a7ea63be57ec588c58bb611f248f8923b1783da047898f71a4712e117b70e2bd14b35dfb4e179940a1f91207f145c"),
                false,
                "primonial.fr"
            ),
            app(
                "com.sdccu.olbmb",
                sha512("7ae170ce6904d2b04d24a15442b4028b16b3c01b25b34a627a16bee4f84e992e553d46308653385781868bf76326351ae19fa59b272a4cc6403df8dc16a654b5"),
                false,
                "sdccu.com"
            ),
            app(
                "com.td.mbna",
                sha512("f761b99460f0090606937a3f177a254dd399486d0024b5f99cbbe9d8708d64689fa9a09c3784cd8ef60c42e35f529c7cee6bf06242410a2f7391e5711e785015"),
                false,
                "mbna.ca"
            ),
            app(
                "com.engie.particuliers",
                sha512("6a1a42c2fd9fbab96656179cb6c120ddcdafd6b428984078b0da56342ad6ad6d5e92c331171614aa3d895bec52e599a01ac0112f64d19c5c9f077fe936dc4fa5"),
                false,
                "engie.fr"
            ),
            app(
                "com.bbva.compassBuzz",
                sha512("a208db935bb3a7ef444c2eedda6dd74c507ceb75c8294b36708fa0c8a350cc67138e806429af3ca139103b546c39a759e57bf705fe942fd15e54d4725c63652b"),
                false,
                "bbvausa.com"
            ),
            app(
                "com.coinbase.pro",
                sha512("7a6984288270596b6fd7b74157a0ad238c1fe428c34d8c89504220c7e931bd8db69402d245126fb01d159efb7ef9a5c2dcb2a800b2b09269ba11aca9b8180232"),
                false,
                "coinbase.com"
            ),
            app(
                "com.amtrak.rider",
                sha512("70ba8db13fa18313ff5c4737af12dabfd7408a62340b371288bc60a626560278bd624352185ec532c8a66d5acda8ea497aab0919064b17ec33569c45ae98ffd0"),
                false,
                "amtrak.com"
            ),
            app(
                "com.pcloud.pcloud",
                sha512("986a150dc5d5e882d2c0925ccc7d7233eab4a3b4c491a4cc6983899a182cd03cbdc582f183297ac361897546d7b44a28c36cb505c85004e653db2527bba91eaa"),
                false,
                "pcloud.com"
            ),
            app(
                "com.fitbit.FitbitMobile",
                sha512("bec11c99bd57c1e4f239a9843ae9ba8c39714ddc76c1d079a49d78cffabd3e4f8077acb465b60c7305fd6b4687e90bdfd73adeb4094e11b3c90b58f6f2ac7d4f"),
                false,
                "fitbit.com"
            ),
            app(
                "com.navient.navientloans",
                sha512("4c0fe923382763dc0ab622cbd9ad9cc4e0d82774a195a71a56709552a6454eb3e3c7c8a2b4da9f2545b184528075cea1acbc46a62547ea11a2054c7aa867bac8"),
                false,
                "navient.com"
            ),
            app(
                "com.hrblock.blockmobile",
                sha512("d974d7237fec0506a7a751be5020abbca8e3c527ade344ecec664d57debcfbf48d96b84b9a3a5d7322c68767dfc62e265d6e72508ec7e0c95e3aedd094eba923"),
                false,
                "hrblock.com"
            ),
            app(
                "com.onpointcommunitycreditunion5123.mobile",
                sha512("dd2b99e1fb83d1db00a710413bd35cfae4fecc116115cc92e1a04a73b34ad8ef069e4ac2646795743dc6cdd4ca5569f67a351279f09b84fbcdeacc50a49ca8f1"),
                false,
                "onpointcu.com"
            ),
            app(
                "com.wealthsimple",
                sha512("6066abd1feb70569179599027ccbd9aee947c30d7a60faa82bf97d054fb6c5f1c1d62a19b9e8f08aa1c239c70dbfea5462ef70c43b47e5c29657e3dee048e2e3"),
                false,
                "wealthsimple.com"
            ),
            app(
                "com.google.android.apps.chromecast.app",
                sha512("696a69f617980d711da35cce1fe6bddf2f3b76714d51758c5d1cef8f28eb3033371561c693c0819a57d07391a8cde08c99c92688c962252ffab21297e2df8e8e"),
                false,
                "google.com"
            ),
            app(
                "jp.mufg.bk.applisp.app",
                sha512("670c7bc8caa3c53958fa2f1b434ea8c1d241a801989c796fb4bdc33e9e430c80198ef474e3dba77472639941ef062ce502bba2f2355aeceb094658849cba5788"),
                false,
                "mufg.jp"
            ),
            app(
                "com.Travis.mobile",
                sha512("07a2a14669bd3491ec0730706838d763dbf94f213f3704c59e207b1339946d248d1411b9a72a0a3f556f0bc05a07cc441c9c89ed1f31c50e9fb9393344a9ab18"),
                false,
                "traviscu.org"
            ),
            app(
                "com.microsoft.bing",
                sha512("4e7e03f6b5bb6887599f9f567f6caf3c5e22d6650bb4af010ebbbb4a08fcf20ea014cdbe77141650c770c77e5101ad873515dfbe94844efc4f7d0efcceb5c4f6"),
                false,
                "live.com"
            ),
            app(
                "com.meraki.Dashboard",
                sha512("429ae713a4d11da8ee794953eff31993b3dc7cf567951d23f3b747c77de128fc00aa7921e28864279982cb54a7a63f8e09718a693400e508b4dc2f12c3ef351c"),
                false,
                "meraki.com"
            ),
            app(
                "cuofco.mbanking",
                sha512("af1fd60080995dab1cec6835dfaa333a9e98c09ad4b8faf83281b231a531e183b89527c42c22b3e54c291bb4a193d3f49f3fbd7af46f65ea0edae62aee471831"),
                false,
                "cuofco.org"
            ),
            app(
                "com.conigent.WodifyMobile",
                sha512("33032ff973778918723b52e8cb937df9a96280b3bc7332740e921d7f6f7fa4a9c9c458e1491e5153921b5ece2a86d4c7b3c5392433b0ad89b5347895c864bb23"),
                false,
                "wodify.com"
            ),
            app(
                "ca.shaw.android.selfserve",
                sha512("d23efe8d99223ba7e6106a3445d300fe6bccd6b953e39e223caaa8e94ec7745d250441fa0c3b67807fce31de4ff7e69b5d5ae8be47c5e91cb5a403481edd8c2f"),
                false,
                "shaw.ca"
            ),
            app(
                "com.arvest.arvestgo",
                sha512("8ea047f6b26304f5948b68821e5430e0783e9598bc69a905fa18dcb9916a4dcfbf12a509b87ae1aae04cc068c105bbaa0f95e2aee2d6b80054fde8a54666886d"),
                false,
                "arvest.com"
            ),
            app(
                "com.cmcmarkets.android.cfd",
                sha512("884327665cca98cfa0c44212535550f9e7eab0bf6b246357a33fccc80f92d98844a3ff80cde22e5616bb2b676d4737b4141a5f425e552ca60c3883072fede98c"),
                false,
                "cmcmarkets.com"
            ),
            app(
                "us.hsbc.hsbcus",
                sha512("9f501fde9ba91b9a946de2ad278f220340826907507cdd514447e9b46494daeef3f88d2849524c3d196e89accb7d250e0a75b2e54763d749d9f88634a61c7315"),
                false,
                "hsbc.com"
            ),
            app(
                "com.hrblock.AtHome",
                sha512("b26011a635025b31ea39b448429a8eb36b5f5f65dcfd533fb1d4805bc3b4852c61feea29d75ffeb4deae45d85ed5513604299f69997fc4a04e9ec45c638783e4"),
                false,
                "hrblock.com"
            ),
            app(
                "ch.raiffeisen.android",
                sha512("cd42b75d02af10e2025bb7e2ed023ffcb21bb0609706d6f2a1d90aafc8fa7e04ba7cc9a3cd39a8391b93f15d2462cbc4547bec62ce3ea233ff9bf05cb671208d"),
                false,
                "raiffeisen.ch"
            ),
            app(
                "com.google.android.gms",
                sha256("f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83"),
                false,
                "google.com",
                null,
                setOf("gmail.com")
            ),
            app(
                "epic.mychart.android",
                sha256("12e74132bcd6d1c273cfa5be4c821d38f2e09ca8e4956e378efa0eebdd5e9dd1"),
                false,
                "mychart.com"
            ),
            app(
                "com.facebook.orca",
                sha256("e3f9e1e0cf99d0e56a055ba65e241b3399f7cea524326b0cdd6ec1327ed0fdc1"),
                true,
                "messenger.com",
                null
            ),
            app(
                "com.samsung.android.scloud",
                sha256("34df0e7a9f1cf1892e45c056b4973cd81ccf148a4050d11aea4ac5a65f900a42"),
                false,
                "samsung.com",
                null
            ),
            app(
                "com.dd.doordash",
                sha256("936f83b914216d8a87a797effb5ca9d4500bd278d892079fdb0d5d05fef210b5"),
                false,
                "doordash.com",
                null
            ),
            app(
                "com.sonos.acr2",
                sha256("7c34eb3cfbda05faf56e8890a2abbac14b3036e6e4358849b98e8819b6b7b329"),
                false,
                "sonos.com"
            ),
            app(
                "hotspotshield.android.vpn",
                sha256("d830dd0a0b875e965e20e8020d40cb2a08dae0d8eda05c15dfde8ed9ac470ace"),
                false,
                "hotspotshield.com"
            ),

            blackListAutoFillApp("com.dashlane"),
            blackListAutoFillApp("com.dashlane.autofill"),
            blackListAutoFillApp("com.android"),
            blackListAutoFillApp("android"),
            blackListAutoFillApp("com.android.launcher"),
            blackListAutoFillApp("com.android.systemui"),
            blackListAutoFillApp("com.android.settings"),
            blackListAutoFillApp("com.android.launcher3"),
            blackListAutoFillApp("com.android.launcher2"),
            blackListAutoFillApp("com.android.captiveportallogin"),
            blackListAutoFillApp("com.samsung.android.email.provider"),
            blackListAutoFillApp("com.android.email"),
            blackListAutoFillApp("com.android.vending"),
            blackListAutoFillApp("mobi.mgeek.TunnyBrowser"),
            blackListAutoFillApp("com.android.mms"),
            blackListAutoFillApp("com.touchtype.swiftkey"),
            blackListAutoFillApp("com.lge.email"),
            blackListAutoFillApp("com.sec.android.app.billing"),
            blackListAutoFillApp("com.box.android"),
            blackListAutoFillApp("com.logmein.ignitionpro.android"),
            blackListAutoFillApp("com.appsverse.photon"),
            blackListAutoFillApp("fr.creditagricole.androidapp"),
            blackListAutoFillApp("com.google.android.apps.messaging"),
            blackListAutoFillApp("com.sec.android.app.myfiles"),
            blackListAutoFillApp("com.samsung.android.applock"),
            blackListAutoFillApp("com.oneplus.applocker"),
            blackListAutoFillApp("cris.org.in.prs.ima"),
            blackListAutoFillApp("com.forgepond.locksmith"),
            blackListAutoFillApp("com.android.htmlviewer"),
            blackListAutoFillApp("com.ea.gp.fifaultimate"),
            blackListAutoFillApp("com.android.calendar"),
            blackListAutoFillApp("com.google.android.apps.docs"),
            blackListAutoFillApp("com.android.contacts")
        )

        private val knownApps = knownAppsList.associateBy { it.packageName }

        val allPopularApplications: List<App>
            get() = knownAppsList.filterIsInstance(App::class.java)
                .filter { it.popularApp }

        

        fun getKnownApplication(packageName: String) = knownApps[packageName]

        

        fun getPrimaryWebsite(packageName: String): String? {
            return (knownApps[packageName] as? App)?.mainDomain
        }

        

        fun getKeywords(packageName: String): Set<String>? {
            return (knownApps[packageName] as? App)?.keywords
        }

        

        fun getPackageNames(url: String): Set<String> {
            return knownApps.filterValues { it.canOpen(url) }
                .map { it.key }
                .toSet()
        }

        

        @JvmStatic
        fun getSignature(packageName: String, url: String?): AppSignature? {
            return knownApps[packageName]
                ?.takeUnless { it is AutofillBlackList }
                ?.takeIf { it.signatures != null }
                ?.takeIf { it.isSecureToUse(url) }
                ?.signatures
        }

        

        fun isAutofillBlackList(packageName: String): Boolean {
            return knownApps[packageName] is AutofillBlackList
        }

        private fun blackListAutoFillApp(
            packageName: String
        ) = AutofillBlackList(packageName)

        private fun app(
            packageName: String,
            signature: Signature,
            popularApp: Boolean,
            mainDomain: String,
            keywords: Set<String>? = null,
            expectedSSO: Set<String>? = null
        ) = App(
            packageName,
            signature.toAppSignature(packageName),
            popularApp = popularApp,
            keywords = keywords,
            commonUsedUrlDomains = expectedSSO?.map { it.toUrlDomain() }?.toSet(),
            mainUrlDomain = mainDomain.toUrlDomain()
        )

        private fun browser(packageName: String, signature: Signature): KnownApplication =
            Browser(packageName, signature.toAppSignature(packageName))

        private fun sha256(vararg signatures: String) = Signature.Sha256(signatures)
        private fun sha512(vararg signatures: String) = Signature.Sha512(signatures)
    }
}