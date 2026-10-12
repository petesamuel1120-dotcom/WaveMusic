package com.petedev.wavemusic.data.parser

import com.petedev.wavemusic.domain.data.model.browse.artist.ResultPlaylist
import com.petedev.wavemusic.domain.data.model.searchResult.songs.Thumbnail

/** Official YouTube Music Global Charts playlists, verified on 2026-10-11.
 * Covers and playlist IDs are stable; opening a playlist fetches its current weekly ranking.
 * Source: https://www.youtube.com/channel/UCrKZcyOJVWnJ60zM1XWllNw/playlists
 * Each country lists Music Videos first and Songs second, including Global (ZZ).
 */
internal fun officialMusicVideoChart(countryCode: String?): ResultPlaylist? =
    officialChartCatalog[countryCode?.uppercase()]?.firstOrNull()

internal fun isOfficialSongsChart(playlistId: String): Boolean =
    officialChartCatalog.values.any { charts ->
        charts.getOrNull(1)?.id?.removePrefix("VL") == playlistId.removePrefix("VL")
    }

private fun chartPlaylist(id: String, title: String, artwork: String): ResultPlaylist =
    ResultPlaylist(id = id, title = title, author = "YouTube Music",
        thumbnails = listOf(Thumbnail(height = 576, url = artwork, width = 576)))

private val officialChartCatalog = mapOf(
    "US" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn69On1f-8NAvX_CYlx7QyZc", "Top 100 Music Videos United States",
            "https://yt3.googleusercontent.com/fgrJBM5yJ3LeJLtCVuu8CbGEULoe-NEbp_NP3LXsC1bnENIYkjrLCqgUxoxYi8yCdnVpEc2x9iE=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6O1LS0XSdF3RyO0Rq_LDeI", "Top 100 Songs United States",
            "https://yt3.googleusercontent.com/WJB7e2yXsWzRtq-6LTwSVy5QE_3fCWznawXYJDQD6W_gyrzbBx8fqQC0HO9VnT7-ik38BE2k_2s=s576"),
    ),
    "ZZ" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5kI81J1fYWK5eZRl1zJ5kM", "Top 100 Music Videos Global",
            "https://yt3.googleusercontent.com/hvSCNpuBkVnjw4b6PApn54kwQAXSlMdRgWplLH8VzUywCtlExO_x2RN2PTj7fbFlFVDt6z1XBEo=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6puJdseH2Rt9sMvt9E2M4i", "Top 100 Songs Global",
            "https://yt3.googleusercontent.com/ERbHOUrRoLV3mNy-Bha1MyQZUot3UI5UuPlVra9gROFPjqZjn_OlfO0N0zk_MDh2fik4Df-a9i8=s576"),
    ),
    "AR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn403fWAsjzCMsLEgBTOa25K", "Top 100 Music Videos Argentina",
            "https://yt3.googleusercontent.com/z5SnDwi681O7q8Jl6wyJJ5Xz-jeUE_hHHxQDHU4AfptLyX5iKkGKS8g8fh7MZskBYI3IVIIwUVk=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4Kd7YEG9LbUqvt64PLs9Fo", "Top 100 Songs Argentina",
            "https://yt3.googleusercontent.com/9cvGs6S8UMMlglnGzU3lYkqRvvC5oV3Gnd2lCcDQdEoWXAHUwy67EnMUoAyerM9RH1Y_jqhk8A=s576"),
    ),
    "AU" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn44PMHPLYatj8rta8WYtZ8_", "Top 100 Music Videos Australia",
            "https://yt3.googleusercontent.com/veccGZ-wgirM7uyBcoVp1PJms6v3cKkm7JmXDsFz8uLj5rxgQM9TukAOjsHcIWKuC8hKyqerL8w=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7xvYy-bP6UFeG5tITQgScd", "Top 100 Songs Australia",
            "https://yt3.googleusercontent.com/sPkjV2SgcCj8Jh6nSf89rrScYm3BdhMGyn-0UDvEpncyI4WQMAZnwJkyo817lwCKWSNT4Fejpw=s576"),
    ),
    "AT" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn42I7USCyNUQSjm-OlttqQf", "Top 100 Music Videos Austria",
            "https://yt3.googleusercontent.com/M9MjsRQrjwZWHtIg15adC52cci7tY4xWyTR0vNC_C9Bxq17y0DbJc2yrCJjQQ2dQ--ayWDSlNadH=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6fFTVP30alDfSDAkEtHaNr", "Top 100 Songs Austria",
            "https://yt3.googleusercontent.com/xMkszgB2Q9KmlR7hrY2Epmu-RoFkNN-vST9fVwadtwRumCx_eo0rEiCxgczPCv5L7hhtD8OdrE4=s576"),
    ),
    "BE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn47l8EXIwa8SCWWjh79rgMq", "Top 100 Music Videos Belgium",
            "https://yt3.googleusercontent.com/G3WTFMGysBVWrelU_nl1BdxHWbc_15zs_HSVINRd1NPA9F-5Ge_8drOP-W4OZ_bKd58s1WCufAwc=s576"),
        chartPlaylist("VLPL4fGSI1pDJn64Up8Ds5BXizLBFZ922jHj", "Top 100 Songs Belgium",
            "https://yt3.googleusercontent.com/zPfaWmDh3zhxRXSffPr4wOvBTKVn4ZiBSsgYDk7Ri904pPXjmkXtbnF8qT_ntaIsPNOa2cq5YCc=s576"),
    ),
    "BO" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn7ShAOJR2HPZrHb83DpSzV8", "Top 100 Music Videos Bolivia",
            "https://yt3.googleusercontent.com/XlKrPa9jCHSN7def9E8ze6_NkdLLpqgbaTLnqreb5EA-iVMuxFZUVBfoFPIgfbPZ9vEohgtSGg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5Vi4RJX33LnETbjMhmPc9V", "Top 100 Songs Bolivia",
            "https://yt3.googleusercontent.com/m3l-Fyo2R7t2s6--UcVmwzDgptxAl1-7qIZPqQuMXiP2Mk9zuV5VQZFighwsIKIhBTCqtDCstg=s576"),
    ),
    "BR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4Gs2meaJRo9O8PNYvhjHIg", "Top 100 Music Videos Brazil",
            "https://yt3.googleusercontent.com/5JxyemcExG1QrHIVb-0gJapnqa8NvSkIzFu28KflUVh3y5rqfhquC3WAcGGXHZPEGdw62Rk-n7UJ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7rGBE8kEC0CqTa1nMh9AKB", "Top 100 Songs Brazil",
            "https://yt3.googleusercontent.com/QvaNMi9ye2Bitg7ZzwsSQbEbXDWa10mJ_JaWDKPvY1gnp8mrPXWdImqQQlXU_7Mkjky2GeVn1g=s576"),
    ),
    "CA" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4IeWA7bBJYh__qgOCRMkIh", "Top 100 Music Videos Canada",
            "https://yt3.googleusercontent.com/l1oP_ORs-ZrF6rNTC4paqDyg8GwQWR6tWdXFO1o4dxIIvLBykNeU0YriQNhtJOXr1Ua09tkN-W0=s576"),
        chartPlaylist("VLPL4fGSI1pDJn57Q7WbODbmXjyjgXi0BTyD", "Top 100 Songs Canada",
            "https://yt3.googleusercontent.com/GonAzrU7ddxt-WCPqk7tksdcWlvsL9OVNepVe7wCdfcV9Mbjdqj_RfafXs8FKfKLWu7bIEz9wvA=s576"),
    ),
    "CL" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4M3llRxwSebRSrjFqeNN3x", "Top 100 Music Videos Chile",
            "https://yt3.googleusercontent.com/Vel9rpZC8w9A4RuIeUe66btfNXMaigm4kZtUnrJ6IF6yRtJ0SfCA2mGDnPY1i5hW41gwx5vnpU8=s576"),
        chartPlaylist("VLPL4fGSI1pDJn777t00zYu_BKjXHUdhkXH9", "Top 100 Songs Chile",
            "https://yt3.googleusercontent.com/6bBvoBICf0hpiwgGpx29yj-lrq9ymakCpDrOGp3wttWg9AEelbHM8dVaGe8fROXdYK-GXitZs64=s576"),
    ),
    "CO" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4ObZYxzctc1AM45GSWm2DC", "Top 100 Music Videos Colombia",
            "https://yt3.googleusercontent.com/UayzYGb64hzAoS89DzhUzWF6fZICtYwGkuRJd7wilAnDmQKIGRpQEmJH-s1uNM264SVHrIy9nQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6CW97F1vSZOkoU7k7VsYk9", "Top 100 Songs Colombia",
            "https://yt3.googleusercontent.com/ASVHsBWSKXHjNZxTF_B1xniVgD5rYOTh8TMGKf3b-nfJhYP4PjHraYeL2F-PiYsNNmZ1RpORoT4=s576"),
    ),
    "CR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5DbEh-PXgn9ZInq-F3NSOZ", "Top 100 Music Videos Costa Rica",
            "https://yt3.googleusercontent.com/x7YE4UqH4CMb1t0nTKUkoCpzT1gs3qJ52KTwPPeMYBwDM4Of54iYNqMm2VlJpQ63GDxlq4yXMts=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6U9fUfBkfy3uyXE7Rtvo4b", "Top 100 Songs Costa Rica",
            "https://yt3.googleusercontent.com/Y0hKD5pDSUPIOhZdCuphYKwvr5c9U1VwgauA6XORlMTesOho6fxSgJyytP3VYpn2ZfKUcrh6qcG1=s576"),
    ),
    "CZ" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4PsD5Tua9nTgnPsP0o9_0k", "Top 100 Music Videos Czechia",
            "https://yt3.googleusercontent.com/5HR6OVmUavgxSXcCDkWtdsgm92Co-kl0VUseTC7wQu3hGTplXblOJroAujKYDS9kU_yg175Bnvg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5wV1AgglmIN_8okwTkz9WT", "Top 100 Songs Czechia",
            "https://yt3.googleusercontent.com/RwrKseH36O35GNArc8qdgKleS9QYg7LO9bFoe0gjfmHhp5Kh0uDHAu124CAG06qWVhrx3rym0j4=s576"),
    ),
    "DK" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4YoCXjBl6kg3DhYgUJTSfw", "Top 100 Music Videos Denmark",
            "https://yt3.googleusercontent.com/TygGzYds-aMeCYxROnhF47LDQjCvc6LXzNfxLt2cMS2QfffddwfpKXaS5ZgQPQlBW8rAAT9N4K0=s576"),
        chartPlaylist("VLPL4fGSI1pDJn51jFsgXEIR7WdKBychJiMU", "Top 100 Songs Denmark",
            "https://yt3.googleusercontent.com/Rcg0KvwDe06rnFV6Yh0ht-rg0bM2p-E-DxXfY8J9mXwanWng-Bpo1iI_aI_rJLH_iJemHGX40rfN=s576"),
    ),
    "DO" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5yRDIISesmCXjor-3lw2ET", "Top 100 Music Videos Dominican Republic",
            "https://yt3.googleusercontent.com/gwXLeSIpx7XwXRwJmzgx1LVwgE5t3NdiXsFhiyuuemMZZpAt2TKoyleDu7mSXbyHoK1jr-Rynxo=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4C36SQoHh9fII-EXde2i3k", "Top 100 Songs Dominican Republic",
            "https://yt3.googleusercontent.com/bjS5iZEDLQ-8DZTCMKd663oMQ18uOmGPKwf4_8L8kQerD7iLEQC5DTW63Eh0Xahf_3YnkzpBFKo=s576"),
    ),
    "EC" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4mwbdPq0EeOiD86MvMtkq0", "Top 100 Music Videos Ecuador",
            "https://yt3.googleusercontent.com/x5jA4WMr6kDYQgI_2JdFiC2spFNu80ar7J1F4Xbtrm7Sc2BF5VdscVIPQiWEyMbDH6XoUs1kudvG=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7K4bdLZJ5GppzLDAihF58q", "Top 100 Songs Ecuador",
            "https://yt3.googleusercontent.com/TPbOBn_rnra821Y7zFzBp_WT6cr__my-lDYu7Q1Q9tHNk5B8ELFgd4iHEvQwKnSJ2Gpm2CgbTw=s576"),
    ),
    "EG" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4EhpZkSSpdyWUet73FalVU", "Top 100 Music Videos Egypt",
            "https://yt3.googleusercontent.com/Mbsr_rgLWENqTHNozPNyTV5aQ6201jwmngATrbrSYJq8GHvjxmj6rJijCRp6j6GgH_qW-F5YKZA=s576"),
        chartPlaylist("VLPL4fGSI1pDJn510j-1L8bMgKTyeRwPrXWY", "Top 100 Songs Egypt",
            "https://yt3.googleusercontent.com/_MtjUODFNIfTHSUJVwKIkO6bcSM7mKUfZJjYIqx5HsphBQUq_jQy9JRQoMK6KvlEJfp3lklshds=s576"),
    ),
    "SV" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn59f4Ef4W3OgMW7HivJCrCt", "Top 100 Music Videos El Salvador",
            "https://yt3.googleusercontent.com/_EBkvToBBt3XlPHsN9AeSEsaE_jW6RjeWCdcH2mDBTCbE8MAaxH9aDnRi5pauKpf9-wYW8o4las=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6ALv-WRypOl0nGaLgtW6nC", "Top 100 Songs El Salvador",
            "https://yt3.googleusercontent.com/_bcgYAC0kve79OqjSDpV1Wh1CLZh-wrfEU2gyxUsv1hl2kOUZ5W3hWDjpDkfsRtANxbHOsxeWw=s576"),
    ),
    "EE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4fpNbyI8YHStVF-wyzHJtd", "Top 100 Music Videos Estonia",
            "https://yt3.googleusercontent.com/E3krYR-UdGh6SzuKWU_75SA9wVWgesRX4Qg4YGXNV0haxgqa1iMff-frXdqPMp39Ba7GpXJH5w=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7uCBUO9GemJda1xfqmvV7_", "Top 100 Songs Estonia",
            "https://yt3.googleusercontent.com/E8_jLegHTVvkMTuRzWsIbmGZfIUeyeAtJuqyVURMu5cF2iMHp36M_rNeMmchB5naJg7_KxQ7pA=s576"),
    ),
    "FI" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4ogogSnHUTIWMc_b7pHW9A", "Top 100 Music Videos Finland",
            "https://yt3.googleusercontent.com/eE5Z_-_4lRCIW3fkAP2mwF3XBetsv8NPhYrztfaqxhsMRwAUzcgcYXVh9UcgrHZh2M6P4i4uB3fl=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4T5TECl_90hfJsPUu1yi2y", "Top 100 Songs Finland",
            "https://yt3.googleusercontent.com/yzRZT4uCBsUMZ0DOEB-b6B7fuXdN0mW8CZsQ1C2LACEgchaiMlOkQyW8aT2pVuNCY-mulBgp7w=s576"),
    ),
    "FR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn50iCQRUVmgUjOrCggCQ9nR", "Top 100 Music Videos France",
            "https://yt3.googleusercontent.com/xiOwyPwf1eBvyaRQPx8OnvwR7xIwLRebUg3ummLmMi6C2jRX9mnQLLL-OY875RXAeXOWZC3k4EI=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7bK3y1Hx-qpHBqfr6cesNs", "Top 100 Songs France",
            "https://yt3.googleusercontent.com/e3_Gksvf0GQ7hY7AtrVBHdEXLX-2eGeN_OasMUvMNgvv2rIuBBoySEuCHEKtqFULXEcyhAfbKbk=s576"),
    ),
    "DE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4X-OicSCOy-dChXWdTgziQ", "Top 100 Music Videos Germany",
            "https://yt3.googleusercontent.com/vN--DxmSbSp20SPz7XY8NBc_-zSDQa1E4S3i3OmpvRfQJoAOmnvY2iZ2e4AfS-mpxKJEG3QNHw=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6KpOXlp0MH8qA9tngXaUJ-", "Top 100 Songs Germany",
            "https://yt3.googleusercontent.com/NACnfGUq_NtktcjShUXNxkF6pOJk4qug9QYRxkkV38DeGjBz3m3Rc2PW7Nf1FTE6GUyGJMacaZE=s576"),
    ),
    "GT" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4MxjoamEWxTh5J7lw1JZfA", "Top 100 Music Videos Guatemala",
            "https://yt3.googleusercontent.com/d02HSKemyjcFfnAyBGnxj0lo_pe15jdgS05qY_f90NozjEYOxHNAmdtsv7N3HedmxmtMTI_CSf8=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7NCQ_U0nwlhidgZ8E3uBQw", "Top 100 Songs Guatemala",
            "https://yt3.googleusercontent.com/bB8ZMHlzoRVx_pulPP-5l8Df0c6QlZD4fr1bnpaZkJ2xNcDNiQOizAjwdYTSwymiNo6Gu92fOAs=s576"),
    ),
    "HN" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5ESsEdE2R0v1nrLPIeM4Xx", "Top 100 Music Videos Honduras",
            "https://yt3.googleusercontent.com/rnVdEN6jShUkCGUwszqidfafI9_1wPrKsTPNdcaTv2C8PTWBU0wbioT5Q5wz9hR_iDFjKiZa0Bc=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5ZVtAKP9-OKnn09CJ-Znpt", "Top 100 Songs Honduras",
            "https://yt3.googleusercontent.com/eSqCA0nKaNSpP2mJBAyWcBCtgTuFqfyufzUljm5qfM9pPWHcSs_zIsN7MlvuyzheYgjUEcJCnYg=s576"),
    ),
    "HK" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5ZuHumtS6meFhiCcxLqrCb", "Top 100 Music Videos Hong Kong",
            "https://yt3.googleusercontent.com/yXaSs-avFbFGo6rOBXEl-IB2FlxnEQBbNajm-uHRfIpTRoYlQqol7i9R56m6_DrVtLvahc2oTg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4jXaIjA8PNyOkIBmMWjNF9", "Top 100 Songs Hong Kong",
            "https://yt3.googleusercontent.com/aYs6pBsOxm37smsK1F-1Ra3uxqSGNfYm41Deg2AOjPQu1JpikGbNDyJk2zkzZzZcY69e_tTt_rg=s576"),
    ),
    "HU" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6-AkuEzkhgTBJq3Lm0Oolc", "Top 100 Music Videos Hungary",
            "https://yt3.googleusercontent.com/eoxazST_LxRkNDlMu4djZ4U14S3brfEGcTJaiEMmMVAzJlfFJEFjyfF80oIVVjNBN-kDuMWEsQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6K3QY1nHyhOGQqNCBGbMKi", "Top 100 Songs Hungary",
            "https://yt3.googleusercontent.com/bk3A3cd0q2AFj3Vy6jcAdZgehiO-ymognDg6dz5Ktq-r74rej7GEzlsWhoAIv6cCAc8w-vJJwhw=s576"),
    ),
    "IS" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5AT1xUL_xmiBWNqDv33giB", "Top 100 Music Videos Iceland",
            "https://yt3.googleusercontent.com/l5l-nK4Eo_JRjYzQlqCUPGAZert8ayOx73NlXnTjiLfuZol4wZTIHfLIJZ7qfCXFCEYMgqCh9g=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6pwJw_mb31TUqc9C_gpskG", "Top 100 Songs Iceland",
            "https://yt3.googleusercontent.com/6aRD6j6c5WfOVWLbZinIcRQYIZjYwhL2L7r7wRRlr7oOgbXjeE_IZks1tWe13kZBEml0zxGvNHaH=s576"),
    ),
    "IN" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn40WjZ6utkIuj2rNg-7iGsq", "Top 100 Music Videos India",
            "https://yt3.googleusercontent.com/cPDH_lR457B-bknNBlnUOoQ1CzUrQEm1FW2SeJ7czKf6uO77ccS6PLxu9AnRXY0Qhb4A1P7KeUe0=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4pTWyM3t61lOyZ6_4jcNOw", "Top 100 Songs India",
            "https://yt3.googleusercontent.com/GQWAszUe8owu33FWLwuvUtpX-mbnBCvltvqCzpDPc_OydFRswPFF94pguoNv5uljJLSX1SLiftg=s576"),
    ),
    "ID" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5QPpj0R4vVgRWk8sSq549G", "Top 100 Music Videos Indonesia",
            "https://yt3.googleusercontent.com/bSJpzZqDnfYfdjahLLWUiT1j3OroLZlTz7nCJcgys04rD41W8fVok9QWBkfiMyYCbEZN9Wb84A=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5ObxTlEPlkkornHXUiKX1z", "Top 100 Songs Indonesia",
            "https://yt3.googleusercontent.com/n5aisOsANNgHdWMx6fsHiBVw6h-bEf-5ezSct3muTLKnmQL_mupj1oVjpYhl_ZjkGum-WUceXvQ=s576"),
    ),
    "IE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn574980IA4DVKDl8PDskrCj", "Top 100 Music Videos Ireland",
            "https://yt3.googleusercontent.com/EJq4fIsia5r2z0VtePl2tPKkb8zcZETMIpzyBHxgpKsdGFUB5qq6wRvX7499u2H0s2zJm8UCEm0=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5S_UFt83P-RlBC4CR3JYuo", "Top 100 Songs Ireland",
            "https://yt3.googleusercontent.com/a3s7R-5U-rkb0D1GRKFIfbwkeE0mF-mQKmbfyPsyKdSmWstBGljI39X8czYlJfqBAOwsj2NDkw=s576"),
    ),
    "IL" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5xFol0l4GwBnHYtXXMaY82", "Top 100 Music Videos Israel",
            "https://yt3.googleusercontent.com/ipWmybc2A4i9H6Zbk-4FOUkY8g7K_SjqATeIOWUQpcxlIaO-6rIGoxTBv27LuWxTS6nl_PvDt-8=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4ECcNLNscMAPND-Degbd5N", "Top 100 Songs Israel",
            "https://yt3.googleusercontent.com/PHtxy0-zOWXE83UYod9B8kjqS8XanVZJPy6-y3IPHmDDv8xLevA0rcgsZAH0z7YQZHWZ1N9a6rA=s576"),
    ),
    "IT" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5BPviUFX4a3IMnAgyknC68", "Top 100 Music Videos Italy",
            "https://yt3.googleusercontent.com/5x-jeiYELVGyHmQe93Y52gCpHJ1SLIf23f1KwIArSjJqYnZ6Siud5soAt0-yD3c19zGkr78dtYQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5JiDypHxveEplQrd7XQMlX", "Top 100 Songs Italy",
            "https://yt3.googleusercontent.com/Jm0hGbUooMv2nUtoQDzb_4SCEz0ehsxmpFQzPRopQNFja9Qc9fH2LCPgmaElNbw02PI26o3UuQU=s576"),
    ),
    "JP" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5FhDrWnRp2NLzJCoPliNgT", "Top 100 Music Videos Japan",
            "https://yt3.googleusercontent.com/PboQRaMIcSKP_noRKT5wg68RbgZWMxA8CpbTi4UGuH3LqM4WzCZY9ovI783BPIqntvb-eBXL7UI=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4-UIb6RKHdxam-oAUULIGB", "Top 100 Songs Japan",
            "https://yt3.googleusercontent.com/VCD1F0svT53XKdlf2JnffsAbSr7H2EkysAOGooxX3dP2djIX9JJbRLsb3quAldEt2hNRGqW9ng=s576"),
    ),
    "KE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5OYRHJhIu_bu7NRnkZ56ds", "Top 100 Music Videos Kenya",
            "https://yt3.googleusercontent.com/lacQP_8oHHyDTcCH0RchJmcJnTMc25tIodJlyThKuPcAsXWSY8zE7balrCchldWTMZcevqJqM9Mz=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7z-3xqv1Ujjobcy2pjpZAA", "Top 100 Songs Kenya",
            "https://yt3.googleusercontent.com/uv_GGnczUqWfKJW9WzB4jqG2us6wwuugtyl9i0KIlDXnxzD37pxcm2dMPzaJUqiL7Cfix0oekvU=s576"),
    ),
    "LU" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5Zla7aGxRJkvIbikXpf3VR", "Top 100 Music Videos Luxembourg",
            "https://yt3.googleusercontent.com/dDTlkybt0F5oSevWAjntezXjvTallZa2y60PWBdUrvNlUPdxeggXy_ryVc9t5mxYyMDMjC6pHvED=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4ie_xg2ndQYSEeZrFYvkQf", "Top 100 Songs Luxembourg",
            "https://yt3.googleusercontent.com/febDSQPpw4G3-YG9WLHOXUfC49dPDvsoFJIGXS7X5Br7rBnXiZfYEez5Niel9ZWHJz5Tr9-geqQ=s576"),
    ),
    "MY" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6jO_K7Kh2dVe2-Bx-i3ezR", "Top 100 Music Videos Malaysia",
            "https://yt3.googleusercontent.com/ZXPZGTWY4vfs_OSduABE8xy3frE1m5lCahvydmj4kvjngOgCakitT7WemDx4pn856AAotGPKoOg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn75jVGfyUmvCNB21v_Z4-b-", "Top 100 Songs Malaysia",
            "https://yt3.googleusercontent.com/tXMGeL0htGQx-jik7OZJzQFyLN9k2HJ-ODZur8iEm8P94PNXtOr7G1PPouoU5InBy6OGdVvjHFk=s576"),
    ),
    "MX" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5cDciLg1q9tabl7gzBZWOp", "Top 100 Music Videos Mexico",
            "https://yt3.googleusercontent.com/S3_M2ylwOGt8xxD3eraVUQ-MiMAxo2NVpOC85NSfArIK_Om_FZ8lEPdvVhRKwOt7kPDSshn-jtQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6fko1AmNa_pdGPZr5ROFvd", "Top 100 Songs Mexico",
            "https://yt3.googleusercontent.com/QGcpEDftN3qSIy9l85RZ1w6IwHk4whLHongs2ro9Sp9_WpMzdDAqul0QQA_jcsgRvjaYpNM37Fq3=s576"),
    ),
    "NL" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5i2QIxSEhPqSzhqsWhhrBJ", "Top 100 Music Videos Netherlands",
            "https://yt3.googleusercontent.com/QV2JJpYAGuP6klLr20WEMvrMP7mjLJkEH0hpkR89636LeTpwSYYc1EzR0lNu2mhH6MguHEl9Ng=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7CXu1B1U0lYQ0qfPB9TVfa", "Top 100 Songs Netherlands",
            "https://yt3.googleusercontent.com/bnQ1CPspUrPmS6xe1gnxjAmqlkXSpZy8SkmXGhu70DKHh0g38wsWGirRlKsKmlMVfF6Fm8Y7bFJp=s576"),
    ),
    "NZ" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5yaX2-KEdvxQK0w938c-NX", "Top 100 Music Videos New Zealand",
            "https://yt3.googleusercontent.com/OV5t22Ye09fAOngguX-efelpKTG8Tk0ZZiskKifhJfG4htkpCMMyZs6iEIx4fB2gCLYkuqFTl5jM=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6SZ8psSiS6j-QgUACJK4gC", "Top 100 Songs New Zealand",
            "https://yt3.googleusercontent.com/aTTf_FX_TRK1Fo4qetX1ZRP-EmqcSQvg9s184woeKS0FpT09_ryOT7sKLxv57P83gSnxp0QM7A=s576"),
    ),
    "NI" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn49ZZQP_cqjJ6NvkJ-qf9sn", "Top 100 Music Videos Nicaragua",
            "https://yt3.googleusercontent.com/yZYtPo91DOV1utHs07iAZfC-HzLbOVy7qFujOXbg2AIMlHBLdi85hqQE3WSAElM6m_6lqTQIfg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7eCAxG3AuCuottnW_D5C5w", "Top 100 Songs Nicaragua",
            "https://yt3.googleusercontent.com/UfWrZCKpSlKqxhc2KARpJqlvuwPbDetwD2P8Wck46aU6J2-UPniyMOfhc9yMdSeD_2fB-rRgg6A=s576"),
    ),
    "NG" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5dHScZlGIe6TEoGzFv_qZE", "Top 100 Music Videos Nigeria",
            "https://yt3.googleusercontent.com/Kf96xBXNMAZgShO9EuLAlYCvhXoU-eTgRmMYDjvDSdwksbhXkDGmHz8IzmIhStI_nIXk6z8Yctk=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6Au0oeuQPsd1iFyiU8Br9I", "Top 100 Songs Nigeria",
            "https://yt3.googleusercontent.com/T-sOzv6AwjiSmNP6HnmJ0r50dHhmE9K3vwL2sNyeorT0H3GY231bYxyOsrRhkKlapsr-uBiyBXw=s576"),
    ),
    "NO" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5qlG8HM7Iq54JE8SROhAvM", "Top 100 Music Videos Norway",
            "https://yt3.googleusercontent.com/czPkSoN5ZW2U7v1ACgvSDymcRY7PwdsYZ9zzqvpYm99x0hLHDh_eB0_SNdDZUZ1eRRffLVbtKOM=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7ywehQhyuuPWo3ayrdSOHn", "Top 100 Songs Norway",
            "https://yt3.googleusercontent.com/JNheALmu0a8vv0WBVO9w3pfKvOSFG2VHzqYBh3S2S5CovV6h5Ymjictz7nw8MnXQe2S9owp_s88=s576"),
    ),
    "PA" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5HtzpSBgS58MlBzOfKgqGw", "Top 100 Music Videos Panama",
            "https://yt3.googleusercontent.com/Vdmro4W_JWUxv481EeVouOLBW9PgvRVgdAvtixfa4YGj3zkV-9z7noLc7D2yXouqDgrO7Ve0Rp4a=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4G4B-V4UTrxD7l5mE9cPS-", "Top 100 Songs Panama",
            "https://yt3.googleusercontent.com/QdLBtU4tS-Rk5uo_Cjzv_ZUUE1nRX8weq1lxHSi-VqRlyk-rKzJPZaT76G8qY20gW1xXRbLJVEo=s576"),
    ),
    "PY" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5rl3PizG5yhj1R97Jl4vBq", "Top 100 Music Videos Paraguay",
            "https://yt3.googleusercontent.com/htXCOEjTNMxFGPWzuS-DybiTLJ9jPSDKCY2Eab9u2X0QSJN4MwUFc8xmIJsoeVPNCdMvvi2hh6dB=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5G0B8V2PSgs7O9EA4gF5m_", "Top 100 Songs Paraguay",
            "https://yt3.googleusercontent.com/2dHEST_OiozvJbsot0311xa5qG9KEciJHn6xgtdl8t3vzLaP-Qn1gRoXt7cPRNjcrReSLJh2Gg=s576"),
    ),
    "PE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn61j743B9r2LNeLCUUZsRMV", "Top 100 Music Videos Peru",
            "https://yt3.googleusercontent.com/ld20bygvD3W85MkqlkPKCzmp23M4COZTMgx-IXTQzfhA1YnaR6DcXtupOPMsfyuBObjgu3CnzA=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4k5jOJjYpq8pluME-gNAnh", "Top 100 Songs Peru",
            "https://yt3.googleusercontent.com/gTuJwFD7XRYIvojRCKMwmCkKQ-gFDWmxck8T59OvC7iFXq1o8UKU4B4Q0vmI01xGUn8UFLjSlipF=s576"),
    ),
    "PH" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn77tAbm8_DdZ0KdWSWYJlS4", "Top 100 Music Videos Philippines",
            "https://yt3.googleusercontent.com/nJVDOfPOPzMINVxF9a4Fi_BhEDJ8Y4-dKx-p3pifpozasaw3cbnfn60BW3r9JhGP061w_boLQlU=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4TGgtEDLeFOonvba_jrsdc", "Top 100 Songs Philippines",
            "https://yt3.googleusercontent.com/85JAZvTIviZTBX1hgKMCsBezXTpG8GwFuc3qyXHmc0eLvW43gEphRMTSMheS_94J9NF6twbdMw=s576"),
    ),
    "PL" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn69d7Zwro65Q7ORLxFVqr_U", "Top 100 Music Videos Poland",
            "https://yt3.googleusercontent.com/K5EQDo2DToHi1EcC6Jk9uCTxsjVPzvBsSQ6_qXzuxBVJ6cQCGseJnCg0ZO3unppJtWO22cZVEw=s576"),
        chartPlaylist("VLPL4fGSI1pDJn68fmsRw9f6g-NzU5UA45v1", "Top 100 Songs Poland",
            "https://yt3.googleusercontent.com/p0_LJqgjSMX-IAbctymx74NYY2KuUU4JLlnUilf6NVya7Jb9JhAHKjh7a9-bTdQA6agSA98fUSg=s576"),
    ),
    "PT" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6G_VdIB6wxGYzuai0iA1hC", "Top 100 Music Videos Portugal",
            "https://yt3.googleusercontent.com/Jm06SbTQ3yK7zFzQhv3muAVtrDtomwba6NsG8M3KT3YtIvliza52XiH5veUnRq1mxE5un3AH--8=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7H0X0bZN4C-I6YeldOvPku", "Top 100 Songs Portugal",
            "https://yt3.googleusercontent.com/4hw-ynTjyy6nwOjDQqnp3S8GkcBn-kjmky7rLX3bRJSzK6EyMDe12YKmYpcMOXqfCHIk67Fm1g=s576"),
    ),
    "RO" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6L8lQpfbnXpXUR71uksmP2", "Top 100 Music Videos Romania",
            "https://yt3.googleusercontent.com/4rN22r62jnHd2v6zjgTtqLzURgPhLUWl83yusuY6146ONy-Z4w-MotGjBnMGdGGD_DKFwCCYqP6s=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5G2T6hrqwSS7ajUA7y4S5l", "Top 100 Songs Romania",
            "https://yt3.googleusercontent.com/9JMYiwy0eYq9mhHokvDSZe2vPqlqq4xAgUdNObNdylSMEEi9FaHnyojvGd1_Kt3MgJpVZwhFVbFx=s576"),
    ),
    "RU" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6cLcPmcc9b_l8oM0aJtsqL", "Top 100 Music Videos Russia",
            "https://yt3.googleusercontent.com/8FyevNWHs2OMrEXkRRedD_JENx3J5kXtGnixxbv6MFcf9sV68tQHFK8WRxfqHOuY-DOve-9lcAY=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5C8dBiYt0BTREyCHbZ47qc", "Top 100 Songs Russia",
            "https://yt3.googleusercontent.com/vUXrxZK7bdp-OsBZ_cOAX6PhVsihtDmXei2QWXNbkLLmRM9eJBwBwr19mEZFYKunZSyHzhvIPOo=s576"),
    ),
    "SA" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn7b8BNLVP8XUrJCQp_loKZT", "Top 100 Music Videos Saudi Arabia",
            "https://yt3.googleusercontent.com/-zLTQqstSfvd7Z4NjXFJgsm9XS8SPCcZU-afOzHJL46kXtLlXA6hAwweFqVrRjOHreWjronN9w=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7xNK-XdqvCsqa7I8Nx3IyW", "Top 100 Songs Saudi Arabia",
            "https://yt3.googleusercontent.com/0LFdOjhATm4M71aMOlxU4f-dgLpgPgb9RsT2WaGS7NcEXF0e_zD57tb0q36Xqsr6RVNkXoqWzC0=s576"),
    ),
    "RS" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6WdZq272-vbCc5SJ1zxzbC", "Top 100 Music Videos Serbia",
            "https://yt3.googleusercontent.com/nncohUjONmN5ZFEVnM_NPkJhriX64MXkpWFeC5plmyxQW1VE7PzgEhmWuKEWW2Z1yVROc7MYyP5l=s576"),
        chartPlaylist("VLPL4fGSI1pDJn79dpGvfySMY9w43BluD4lI", "Top 100 Songs Serbia",
            "https://yt3.googleusercontent.com/3olMr8BJewErrhxOrdayg1BKcZZVmFmTvxYwd_Fllk6lS_dDLTFIP7MLkrAtxQ6tjEPeFqy8N6c=s576"),
    ),
    "SG" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6NXctD9ORkRNLT7DwyhIOw", "Top 100 Music Videos Singapore",
            "https://yt3.googleusercontent.com/EuFntNGU7QCyrDHc6N9ginhg7bJhLDGQefkEsPNtCIS7T0WFQY5o8-xqNYJC7VfJBYrtmND-PFs=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4o2QztY4I1iUtmlVWVp8Ta", "Top 100 Songs Singapore",
            "https://yt3.googleusercontent.com/pgQfaZ8Qemnk6YddMuHQikyc9eenZwz35uZbCGzP7GfbOm3Qw3h1XByzBA8XY4ofsm55HBAOhXU=s576"),
    ),
    "ZA" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn79YvDK-Dq95SAW1V28wnns", "Top 100 Music Videos South Africa",
            "https://yt3.googleusercontent.com/obnITFVV6UfF8x0NkUD4GXaE33rBDmKuzKpPJYFpVefs7YEjCr-BbaQzeLYpoaF6HayO_A3bOhQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7xvqMZR_9OgljLcMQpuKXN", "Top 100 Songs South Africa",
            "https://yt3.googleusercontent.com/AwC2D6u8Lq8q3Jw0AfjvbbPlY7HatptQfr3d2WbK_vSQOfG8knKy0rsFaXbCmqtkmUMUuKjVJfs=s576"),
    ),
    "KR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5S09aId3dUGp40ygUqmPGc", "Top 100 Music Videos South Korea",
            "https://yt3.googleusercontent.com/v1Jqjczt9XOqW7aiEsarzxpv-xmNgJm2gDkXuinwJcblTgq4BYEqfWoYnB_yOWHjiuuRSc58NI0=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6jXS_Tv_N9B8Z0HTRVJE0m", "Top 100 Songs South Korea",
            "https://yt3.googleusercontent.com/U6huZ5AqF12WUuCPy1mrzuUVcckTpKekEK8QUaaRwh0NK_eJ2Z9S_npubA_cfCoXv2aJfKZa4Ws=s576"),
    ),
    "ES" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4jhQB4kb9M36dvVmJQPt4T", "Top 100 Music Videos Spain",
            "https://yt3.googleusercontent.com/hpczKBaKKo5SZEQKzLyFogW_A8OKa6v72ZNTsDKBH9AQca3tjL55RyV1MDjeUB8x_dn95xe3Nw=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6sMPCoD7PdSlEgyUylgxuT", "Top 100 Songs Spain",
            "https://yt3.googleusercontent.com/nRSVMkXNsr5U91ZvVRySoGHPgdHSY7Qzs-5SL0kGNAL7-5Fc6qpAWN1w3iYHFTWXxDXlTEeTdTs=s576"),
    ),
    "SE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6l_eirqF_T40p1B8eJg2Pz", "Top 100 Music Videos Sweden",
            "https://yt3.googleusercontent.com/smRJDQrCFNx1yZU4MFQg8K13v6_2VWuswB0R7lPOL_RuhXIhdB6gj0GCn1y7JOpTG5cOO5Kb80lf=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7S_JFSuBHol2RH9WphaqzS", "Top 100 Songs Sweden",
            "https://yt3.googleusercontent.com/ynUF9Bfhw3qduNP6XAufGk4t9ThId8Z8SeYlEhLjVoXva8PUxGdINUiKRpmJZc9m8F2Kg5pmLheD=s576"),
    ),
    "CH" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4KBb656ZmzFTCGK0eAv5bu", "Top 100 Music Videos Switzerland",
            "https://yt3.googleusercontent.com/y85kchROtaPvR-1tTjaVrNGCk2Mmwndavf8IQ4sn-rsPr9UNCesLm-neT7YGpWSFer4lvczCgMQ=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6Nhmcqn4xr769wwoMmS3DI", "Top 100 Songs Switzerland",
            "https://yt3.googleusercontent.com/9qfhfz5SBMcyxhERvOJ4DXmrYQtcbFWZHPs5-qX_cXexPKdmSHaXvmAOQsDvBvS0RfzGtUjJ8g=s576"),
    ),
    "TW" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4eKyK8APGwl0S0wgyHvQyU", "Top 100 Music Videos Taiwan",
            "https://yt3.googleusercontent.com/2k9ezdUvtELBX7MLQ3huyLcOzpQM6KoFvubXLsUedTC5JZ7DhlLyGkNJn8513KOOu6OTdeeN5w=s576"),
        chartPlaylist("VLPL4fGSI1pDJn68Qgd3kW_hKrqMhxAHz62W", "Top 100 Songs Taiwan",
            "https://yt3.googleusercontent.com/MalidCa0cA0Z1QsP79m0j6knxwVQi7fYzTjbH9Yoib6cHdP9SGu1Mdc36NB5yF6HbK7daBH7erw=s576"),
    ),
    "TZ" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn7-3qlPahSCN5PagP0L1p6r", "Top 100 Music Videos Tanzania",
            "https://yt3.googleusercontent.com/nAU-eurjCb_1-iNMhRicw3UMgXlRhEj6tGQ-ytViRkGi0EONTrpT6_EwuOZtRpIBISwFAHqfcjJ-=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4CI0qH2JZYs2qGXo1itpCG", "Top 100 Songs Tanzania",
            "https://yt3.googleusercontent.com/73QGfN37XS9GydXAtYFEwjHD6P6j4pe0DKQtOWQelaPA0VMMHYw3F7tvPLYoauQ_tdfrbXU0ZsRN=s576"),
    ),
    "TH" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5J-6eohuDgVr8lTdRpcVQo", "Top 100 Music Videos Thailand",
            "https://yt3.googleusercontent.com/B672P-BuDVbdxWfSo2AzKix_Igv9BSp_IQrlL4lL4BzoROiOqdlfyHoX24q1iiH-7I7KudpMlrU=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5gHpWhR17s41BbppFJFf08", "Top 100 Songs Thailand",
            "https://yt3.googleusercontent.com/kvv68iTFNswEl3vkU3abZjEGc8lT8LoqzvwA2UcfrozJrv4x-TtA8RpoRE16y8IrG0lPia8zSPQ=s576"),
    ),
    "TR" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn6rnJKpaAkK1XK8QUfa9KqP", "Top 100 Music Videos Turkey",
            "https://yt3.googleusercontent.com/IzU_7xQQA8TcllQlT5bE5nGIh86ObOL-rwzp-5ezBu3KRgY7EA3gARbjGkrn9zUsjR61pM3jgeE=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5tdVDtIAZArERm_vv4uFCR", "Top 100 Songs Turkey",
            "https://yt3.googleusercontent.com/QCG3M6JAHGR4nYepsACJ3UwNHtsEemcXkEXLTi2JEGaoxgzJxvzJTWuWKLr_Ua6u9kAcZ083A-M=s576"),
    ),
    "UG" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn75xappx8QlV4-0nuyXlDAr", "Top 100 Music Videos Uganda",
            "https://yt3.googleusercontent.com/4PXU_QXu2R_7WlzExUnG3uejyViX7lAeL9Eid_FMYr3Ie3R6Bbft67NsKZ-EshR4emj34BZdzJM=s576"),
        chartPlaylist("VLPL4fGSI1pDJn56127QXqxGADbedOpL5z5R", "Top 100 Songs Uganda",
            "https://yt3.googleusercontent.com/bfs0owkrNXKTCRZRGA3hj8YpsCUi8sR8bsFN-1_p5JQM4qI4xChkDyVllkF1Li9DNq6oFSsPfwA=s576"),
    ),
    "UA" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn7524WZdmWAIRc6cQ3vUzZK", "Top 100 Music Videos Ukraine",
            "https://yt3.googleusercontent.com/U62WhnthBOZ8g5iTkQXzI3eeqbdHmgbgw9KOa7ru_5HNFT4i0SqvOBk-t39W4YtgQZ8_OD0MtWJl=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4E_HoW5HB-w5vFPkYfo3dB", "Top 100 Songs Ukraine",
            "https://yt3.googleusercontent.com/lZxi4GPt5gDk3BBusdavj3jaz8rYvcR_vLzGGR0xZqxM_k0Lv6dcKJdduiGg4v3HaBmMtBdvKg=s576"),
    ),
    "AE" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4CDqdXJ4xP78Hh7X72vIXM", "Top 100 Music Videos United Arab Emirates",
            "https://yt3.googleusercontent.com/pwYFkbb1KaqkIN6w7s4YnnlUC7LoVenlRPijMiNEhivLT2BZ4brTms0JxywYzoEuXhirKq5JFcA=s576"),
        chartPlaylist("VLPL4fGSI1pDJn71VxNxT-PpECxHCVv8T-oX", "Top 100 Songs United Arab Emirates",
            "https://yt3.googleusercontent.com/Egrs0F1efT9C7tWFZgyn-_z0K5boMWPH3Gz60nbzB1ugZbPvspmbyPWllBsfF7vj4bJoWBT9YT0=s576"),
    ),
    "GB" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn688ebB8czINn0_nov50e3A", "Top 100 Music Videos United Kingdom",
            "https://yt3.googleusercontent.com/6b7w0oC437onRB_Ob6JgfszzfLF3PEqwMtRDXmI7yeSgv2S4VbL6KUPWXiMe0oATNQ2ZAuzUf1k=s576"),
        chartPlaylist("VLPL4fGSI1pDJn6_f5P3MnzXg9l3GDfnSlXa", "Top 100 Songs United Kingdom",
            "https://yt3.googleusercontent.com/xArplZuSyS327PS9yC5f351X3xEksXUFrb6Ab6ubtcwLi8yHmR32ayGt29fMFocByTUpnCZe2w=s576"),
    ),
    "UY" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn5giLQO3qUCqpp_MSDRmJNA", "Top 100 Music Videos Uruguay",
            "https://yt3.googleusercontent.com/NOgHeJxmzBZIdfWkyhDsnbEhK8vBZxWSqU_IY0sbqZ0SjWiH2ZNXqTtByA6HobNuY_lV2U58D7w=s576"),
        chartPlaylist("VLPL4fGSI1pDJn5caN5mlO8NWCPSyuHkQANg", "Top 100 Songs Uruguay",
            "https://yt3.googleusercontent.com/W-7hqBoHoSL_PObiSqGowTbyFArjjbY3MDMWL_Zj_xYL599UWJjbNPbhOCvxqV3MHtDRTru546Q=s576"),
    ),
    "VN" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn4FPCRZtojwqQro5GPY6cuV", "Top 100 Music Videos Vietnam",
            "https://yt3.googleusercontent.com/XCFq99oNITNHrR1xVw0Sw5_DnR4Pbpu-1NcqiaZs7_HZ9AVR8mJDcP8mrQP6JPuvWf3-SCahVrM=s576"),
        chartPlaylist("VLPL4fGSI1pDJn4bRKr6tjRWaGlqRg_zY_is", "Top 100 Songs Vietnam",
            "https://yt3.googleusercontent.com/4WQHi6rNMQaGMsAMf-vGMF_QoUg02qyLN6Pz-xUQ37U56gfc0txaRxHbT5Oa9uR4_hc-CEiDKjs3=s576"),
    ),
    "ZW" to listOf(
        chartPlaylist("VLPL4fGSI1pDJn7HuQm191bZDx7ZMMgk9-Bp", "Top 100 Music Videos Zimbabwe",
            "https://yt3.googleusercontent.com/Pxpca5O7E9oGxfGbn1JuRZr1kKRygn5cDj-DwovBbso7CDE2izc9vdYQrrC5z2miG7qy5PAY1cg=s576"),
        chartPlaylist("VLPL4fGSI1pDJn7PWidyUayXX6-josrejRMG", "Top 100 Songs Zimbabwe",
            "https://yt3.googleusercontent.com/5D5aLwEUVndW0-7SxFVpKPnSs4K1DKJ9ocCfaxgNHfquqpKBn8U1ZRdSortVGmv5vcNbHT1b1Q=s576"),
    ),
)
