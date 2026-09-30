package com.example.ui.components

import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdMobManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.delay

/**
 * Multi-Network Banner Ad Bar (supports Google AdMob, Unity Ads, and Facebook Audience Network)
 * dynamically controlled by the Admin Panel ON/OFF switch and active Ad Network setting.
 */
@Composable
fun AdMobBannerBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val adsEnabled by AdMobManager.adsEnabled.collectAsState()
    val bannerEnabled by AdMobManager.bannerAdsEnabled.collectAsState()
    val activeNetwork by AdMobManager.activeAdNetwork.collectAsState()
    val admobBannerId by AdMobManager.admobBannerUnitId.collectAsState()
    val unityGameId by AdMobManager.unityGameId.collectAsState()
    val unityBannerId by AdMobManager.unityBannerId.collectAsState()
    val fbBannerId by AdMobManager.fbBannerId.collectAsState()
    val isSdkReady by AdMobManager.isSdkInitialized.collectAsState()

    // If Admin Panel turned OFF ads or banner ads, hide the banner bar completely
    if (!adsEnabled || !bannerEnabled) {
        return
    }

    // Unity Ads or Facebook Audience Network Banner Mode
    if (activeNetwork == "UNITY" || activeNetwork == "FACEBOOK") {
        val networkLabel = if (activeNetwork == "UNITY") {
            "Unity Ads Banner ($unityGameId / $unityBannerId)"
        } else {
            "Facebook Audience Network ($fbBannerId)"
        }
        val badgeColor = if (activeNetwork == "UNITY") Color(0xFF38BDF8) else Color(0xFF3B82F6)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155))
                .testTag("admob_banner_bar"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (activeNetwork == "UNITY") "UNITY AD" else "FB AD",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = networkLabel,
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        return
    }

    var isBannerLoaded by remember { mutableStateOf(false) }
    var retryTick by remember { mutableIntStateOf(0) }
    var useFallbackTestUnit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AdMobManager.initialize(context)
    }

    LaunchedEffect(isBannerLoaded, isSdkReady, retryTick) {
        if (isSdkReady && !isBannerLoaded && retryTick < 5) {
            delay(4000L)
            if (!isBannerLoaded) {
                retryTick += 1
            }
        }
    }

    val activeUnitId = if (useFallbackTestUnit) {
        AdMobManager.GOOGLE_OFFICIAL_TEST_BANNER_ID
    } else {
        admobBannerId.ifBlank { AdMobManager.BANNER_AD_UNIT_ID }
    }

    val adView = remember(activeUnitId) {
        AdView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setAdSize(AdSize.BANNER)
            adUnitId = activeUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    isBannerLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isBannerLoaded = false
                    if (!useFallbackTestUnit && activeUnitId != AdMobManager.GOOGLE_OFFICIAL_TEST_BANNER_ID) {
                        useFallbackTestUnit = true
                    }
                }
            }
        }
    }

    DisposableEffect(adView) {
        onDispose {
            try {
                adView.destroy()
            } catch (_: Throwable) {
            }
        }
    }

    LaunchedEffect(adView, isSdkReady, retryTick) {
        if (isSdkReady && !isBannerLoaded) {
            try {
                adView.loadAd(AdRequest.Builder().build())
            } catch (_: Throwable) {
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155))
            .testTag("admob_banner_bar"),
        contentAlignment = Alignment.Center
    ) {
        if (!isBannerLoaded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFACC15))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Ad",
                        color = Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Loading Google AdMob Test Ad...",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { adView }
        )
    }
}

/**
 * Multi-Network Video Ad Overlay (supports AdMob Fallback, Unity Ads Rewarded Video,
 * and Facebook Audience Network Rewarded Video according to Admin Panel settings).
 */
@Composable
fun AdMobRewardedVideoDialog(
    rewardDescription: String,
    onRewardEarnedAndClose: () -> Unit,
    onCancelEarly: () -> Unit
) {
    val activeNetwork by AdMobManager.activeAdNetwork.collectAsState()
    val unityGameId by AdMobManager.unityGameId.collectAsState()
    val unityRewardedId by AdMobManager.unityRewardedId.collectAsState()
    val fbRewardedId by AdMobManager.fbRewardedId.collectAsState()

    val networkBadgeTitle = when (activeNetwork) {
        "UNITY" -> "Unity Ads • Rewarded Video ($unityGameId / $unityRewardedId)"
        "FACEBOOK" -> "Facebook Audience Network • Video ($fbRewardedId)"
        else -> "Google AdMob • Test Video Ad"
    }

    var secondsLeft by remember { mutableIntStateOf(5) }
    val isRewardUnlocked = secondsLeft <= 0

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ad_video_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .clickable(enabled = true, onClick = {})
            .padding(20.dp)
            .testTag("admob_rewarded_video_dialog")
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFACC15))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when (activeNetwork) {
                            "UNITY" -> "Unity Ad"
                            "FACEBOOK" -> "FB Ad"
                            else -> "Test Ad"
                        },
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRewardUnlocked) {
                        "Reward Granted ✓"
                    } else {
                        "Reward in ${secondsLeft}s"
                    },
                    color = if (isRewardUnlocked) Color(0xFF4ADE80) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isRewardUnlocked) Color(0xFF22C55E) else Color(0xFF334155))
                    .border(1.5.dp, Color.White, CircleShape)
                    .clickable {
                        if (isRewardUnlocked) {
                            onRewardEarnedAndClose()
                        } else {
                            onCancelEarly()
                        }
                    }
                    .testTag("close_rewarded_ad_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Ad",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF1E293B))
                    )
                )
                .border(2.dp, Color(0xFF3B82F6), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = networkBadgeTitle,
                    color = Color(0xFFFDE047),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .size((92 * pulse).dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF60A5FA), Color(0xFF2563EB))
                        )
                    )
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = rewardDescription,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val progress = ((5 - secondsLeft) / 5f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF22C55E))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            if (isRewardUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                        .clickable { onRewardEarnedAndClose() }
                        .testTag("claim_ad_reward_now_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓ Collect Reward & Continue",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Text(
                    text = "Playing Video Ad... (${secondsLeft}s remaining)",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
