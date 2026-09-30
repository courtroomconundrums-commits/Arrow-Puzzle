<?php
/**
 * =========================================================================================
 * JAZ CASH ARROW PUZZLE — WEB ADMIN PANEL (FIREBASE + PHP INTEGRATED SERVER)
 * =========================================================================================
 * Pre-Configured Credentials:
 * - Web URL      : https://cashpuzzle.free.je/index.php
 * - Firebase DB  : https://cashpuzzle-default-rtdb.firebaseio.com
 * - App Package  : com.jazstudio.com
 * - Admin Login  : admin@jazcash.com
 * - Admin Pass   : 90970@Zia
 * =========================================================================================
 */
session_start();

$configFile = __DIR__ . '/admin_config.json';

// [CONFIGURED 1]: Firebase Realtime Database URL & Web Config
$defaultFirebaseUrl = 'https://cashpuzzle-default-rtdb.firebaseio.com';
$firebaseWebConfig = [
    'apiKey'            => 'AIzaSyDASsTjtkxGmJCwU3-1IUwT_a2Hsa9vdDc',
    'authDomain'        => 'cashpuzzle.firebaseapp.com',
    'databaseURL'       => 'https://cashpuzzle-default-rtdb.firebaseio.com',
    'projectId'         => 'cashpuzzle',
    'storageBucket'     => 'cashpuzzle.firebasestorage.app',
    'messagingSenderId' => '433504025632',
    'appId'             => '1:433504025632:web:f43153bf00c8210ea75045',
    'measurementId'     => 'G-F10LV901PP'
];

// [CONFIGURED 2]: Web Admin Panel Server URL (cashpuzzle.free.je)
$defaultWebServerUrl = 'https://cashpuzzle.free.je/index.php';

// [CONFIGURED 3]: Android App Package Name
$defaultAppPackage = 'com.jazstudio.com';

// [CONFIGURED 4]: Default Admin Login Credentials
$defaultAdminUsername = 'admin@jazcash.com';
$defaultAdminPlainPassword = '90970@Zia';

// Default configuration loader
function loadAdminConfig($file, $defaultDbUrl, $defaultWebUrl, $defaultPkg) {
    $defaults = [
        'is_setup_completed'     => true,
        'admin_name'             => 'Super Admin',
        'admin_username'         => 'admin@jazcash.com',
        'admin_phone'            => '+8801700000000',
        'admin_role'             => 'Administrator',
        'password_hash'          => password_hash('90970@Zia', PASSWORD_DEFAULT),
        // App-to-Web Server & Firebase Integration
        'firebase_db_url'        => $defaultDbUrl,
        'firebase_api_key'       => 'AIzaSyDASsTjtkxGmJCwU3-1IUwT_a2Hsa9vdDc',
        'firebase_auth_domain'   => 'cashpuzzle.firebaseapp.com',
        'firebase_project_id'    => 'cashpuzzle',
        'firebase_storage_bucket'=> 'cashpuzzle.firebasestorage.app',
        'firebase_sender_id'     => '433504025632',
        'firebase_app_id'        => '1:433504025632:web:f43153bf00c8210ea75045',
        'firebase_measurement_id'=> 'G-F10LV901PP',
        'firebase_secret'        => '',
        'web_server_url'         => $defaultWebUrl,
        'app_package_name'       => $defaultPkg,
        'enforce_package_check'  => true,
        // Ads Master Controls (ON / OFF & Active Ad Network: ADMOB, UNITY, FACEBOOK)
        'ads_enabled'            => true,
        'banner_ads_enabled'     => true,
        'rewarded_ads_enabled'   => true,
        'active_ad_network'      => 'ADMOB', // ADMOB | UNITY | FACEBOOK
        // Google AdMob Default Ad Codes
        'admob_app_id'           => 'ca-app-pub-3940256099942544~3347511713',
        'admob_banner_id'        => 'ca-app-pub-3940256099942544/6300978111',
        'admob_rewarded_id'      => 'ca-app-pub-3940256099942544/5224354917',
        'admob_interstitial_id'  => 'ca-app-pub-3940256099942544/1033173712',
        // Unity Ads Default Codes
        'unity_game_id'          => '4089461',
        'unity_banner_id'        => 'Banner_Android',
        'unity_rewarded_id'      => 'Rewarded_Android',
        // Facebook (Meta) Audience Network Default Codes
        'fb_app_id'              => 'IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID',
        'fb_banner_id'           => 'IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID',
        'fb_rewarded_id'         => 'VID_HD_16_9_46S_APP_INSTALL#YOUR_PLACEMENT_ID',
        // Minimum Withdrawal & 4 Withdrawal Tiers (Base BDT)
        'min_withdraw_bdt'       => 5000.0,
        'withdraw_tier1_bdt'     => 5000.0,
        'withdraw_tier2_bdt'     => 10000.0,
        'withdraw_tier3_bdt'     => 20000.0,
        'withdraw_tier4_bdt'     => 30000.0,
        'per_ad_reward_bdt'      => 10.0,
        'level_clear_reward_bdt' => 20.0,
        // Daily Tasks Control (ON/OFF & Task Rewards in Base BDT)
        'daily_tasks_enabled'    => true,
        'task_login_reward_bdt'  => 25.0,
        'task_ad1_reward_bdt'    => 10.0,
        'task_lvl1_reward_bdt'   => 25.0,
        'task_lvl3_reward_bdt'   => 30.0,
        'task_lvl5_reward_bdt'   => 40.0,
        'task_lvl20_reward_bdt'  => 75.0,
        'task_ad3_reward_bdt'    => 30.0,
        'task_ad15_reward_bdt'   => 150.0,
        'task_ad30_reward_bdt'   => 300.0,
        'task_ad50_reward_bdt'   => 500.0
    ];

    if (file_exists($file)) {
        $raw = file_get_contents($file);
        $data = json_decode($raw, true);
        if (is_array($data)) {
            return array_merge($defaults, $data);
        }
    }
    return $defaults;
}

function saveAdminConfig($file, $config) {
    file_put_contents($file, json_encode($config, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
}

// Helper for Firebase Realtime Database REST API calls (GET, PUT, PATCH, DELETE)
function firebaseRequest($method, $path, $data = null, $config = []) {
    $baseUrl = rtrim($config['firebase_db_url'] ?? '', '/');
    if (empty($baseUrl)) {
        return null;
    }
    $url = $baseUrl . '/' . ltrim($path, '/') . '.json';
    if (!empty($config['firebase_secret'])) {
        $url .= '?auth=' . urlencode($config['firebase_secret']);
    }

    $payload = ($data !== null) ? json_encode($data, JSON_UNESCAPED_UNICODE) : null;

    if (function_exists('curl_init')) {
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, $method);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_TIMEOUT, 8);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        if ($payload !== null) {
            curl_setopt($ch, CURLOPT_POSTFIELDS, $payload);
            curl_setopt($ch, CURLOPT_HTTPHEADER, ['Content-Type: application/json']);
        }
        $response = curl_exec($ch);
        curl_close($ch);
        return $response ? json_decode($response, true) : null;
    } else {
        $opts = [
            'http' => [
                'method'  => $method,
                'header'  => "Content-Type: application/json\r\n",
                'timeout' => 8
            ]
        ];
        if ($payload !== null) {
            $opts['http']['content'] = $payload;
        }
        $ctx = stream_context_create($opts);
        $response = @file_get_contents($url, false, $ctx);
        return $response ? json_decode($response, true) : null;
    }
}

// Builds the public App-to-Web JSON payload synced to both the Android App and Firebase /app_settings
function buildAppSettingsPayload($config, $packageVerified = true) {
    return [
        'status'                 => 'ok',
        'package_verified'       => $packageVerified,
        'app_package_name'       => $config['app_package_name'],
        'web_server_url'         => $config['web_server_url'],
        'firebase_db_url'        => $config['firebase_db_url'],
        // Ads Settings (AdMob / Unity / Facebook + ON/OFF)
        'ads_enabled'            => (bool)$config['ads_enabled'],
        'banner_ads_enabled'     => (bool)$config['banner_ads_enabled'],
        'rewarded_ads_enabled'   => (bool)$config['rewarded_ads_enabled'],
        'active_ad_network'      => strtoupper($config['active_ad_network'] ?? 'ADMOB'),
        'admob_app_id'           => $config['admob_app_id'],
        'admob_banner_id'        => $config['admob_banner_id'],
        'admob_rewarded_id'      => $config['admob_rewarded_id'],
        'admob_interstitial_id'  => $config['admob_interstitial_id'],
        'unity_game_id'          => $config['unity_game_id'],
        'unity_banner_id'        => $config['unity_banner_id'],
        'unity_rewarded_id'      => $config['unity_rewarded_id'],
        'fb_app_id'              => $config['fb_app_id'],
        'fb_banner_id'           => $config['fb_banner_id'],
        'fb_rewarded_id'         => $config['fb_rewarded_id'],
        // Minimum Withdrawal & Tiers
        'min_withdraw_bdt'       => floatval($config['min_withdraw_bdt']),
        'withdraw_tier1_bdt'     => floatval($config['withdraw_tier1_bdt']),
        'withdraw_tier2_bdt'     => floatval($config['withdraw_tier2_bdt']),
        'withdraw_tier3_bdt'     => floatval($config['withdraw_tier3_bdt']),
        'withdraw_tier4_bdt'     => floatval($config['withdraw_tier4_bdt']),
        'per_ad_reward_bdt'      => floatval($config['per_ad_reward_bdt']),
        'level_clear_reward_bdt' => floatval($config['level_clear_reward_bdt']),
        // Daily Tasks
        'daily_tasks_enabled'    => (bool)$config['daily_tasks_enabled'],
        'task_login_reward_bdt'  => floatval($config['task_login_reward_bdt']),
        'task_ad1_reward_bdt'    => floatval($config['task_ad1_reward_bdt']),
        'task_lvl1_reward_bdt'   => floatval($config['task_lvl1_reward_bdt']),
        'task_lvl3_reward_bdt'   => floatval($config['task_lvl3_reward_bdt']),
        'task_lvl5_reward_bdt'   => floatval($config['task_lvl5_reward_bdt']),
        'task_lvl20_reward_bdt'  => floatval($config['task_lvl20_reward_bdt']),
        'task_ad3_reward_bdt'    => floatval($config['task_ad3_reward_bdt']),
        'task_ad15_reward_bdt'   => floatval($config['task_ad15_reward_bdt']),
        'task_ad30_reward_bdt'   => floatval($config['task_ad30_reward_bdt']),
        'task_ad50_reward_bdt'   => floatval($config['task_ad50_reward_bdt']),
        'updated_at'             => time() * 1000
    ];
}

$config = loadAdminConfig($configFile, $defaultFirebaseUrl, $defaultWebServerUrl, $defaultAppPackage);

// =========================================================================================
// APP-TO-WEB INTEGRATED SERVER API ENDPOINTS
// =========================================================================================
if (isset($_GET['api'])) {
    header('Content-Type: application/json; charset=utf-8');
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, X-App-Package');

    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit;
    }

    $apiAction = trim($_GET['api']);
    $reqPackage = trim($_GET['package_name'] ?? ($_SERVER['HTTP_X_APP_PACKAGE'] ?? ''));
    $expectedPackage = trim($config['app_package_name'] ?? $defaultAppPackage);

    $isVerified = true;
    if (!empty($config['enforce_package_check']) && !empty($expectedPackage) && !empty($reqPackage)) {
        $isVerified = ($reqPackage === $expectedPackage);
    }

    if (!$isVerified) {
        http_response_code(403);
        echo json_encode([
            'status'            => 'error',
            'package_verified'  => false,
            'message'           => 'App Package Name mismatch! Expected: ' . $expectedPackage,
            'requested_package' => $reqPackage
        ], JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ($apiAction === 'get_config') {
        echo json_encode(buildAppSettingsPayload($config, true), JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ($apiAction === 'sync_user' && $_SERVER['REQUEST_METHOD'] === 'POST') {
        $rawBody = file_get_contents('php://input');
        $userData = json_decode($rawBody, true);
        if (is_array($userData) && !empty($userData['userId'])) {
            firebaseRequest('PUT', 'users/' . $userData['userId'], $userData, $config);
            echo json_encode(['status' => 'ok', 'userId' => $userData['userId']]);
            exit;
        }
        echo json_encode(['status' => 'error', 'message' => 'Invalid user payload']);
        exit;
    }

    if ($apiAction === 'submit_withdrawal' && $_SERVER['REQUEST_METHOD'] === 'POST') {
        $rawBody = file_get_contents('php://input');
        $wdData = json_decode($rawBody, true);
        if (is_array($wdData) && !empty($wdData['requestCode'])) {
            firebaseRequest('PUT', 'withdrawals/' . $wdData['requestCode'], $wdData, $config);
            echo json_encode(['status' => 'ok', 'requestCode' => $wdData['requestCode']]);
            exit;
        }
        echo json_encode(['status' => 'error', 'message' => 'Invalid withdrawal payload']);
        exit;
    }

    if ($apiAction === 'ticket_status') {
        $ticket = trim($_GET['ticket'] ?? '');
        if (!empty($ticket)) {
            $node = firebaseRequest('GET', 'withdrawals/' . $ticket, null, $config);
            if (is_array($node) && !empty($node['status'])) {
                echo json_encode(['status' => strtoupper($node['status']), 'ticket' => $ticket]);
                exit;
            }
        }
        echo json_encode(['status' => 'PENDING', 'ticket' => $ticket]);
        exit;
    }
}

$flashSuccess = '';
$flashError = '';
$showSavePopup = false;
$savePopupTitle = 'Saved Successfully!';

// Handle Logout
if (isset($_GET['action']) && $_GET['action'] === 'logout') {
    session_destroy();
    header('Location: index.php');
    exit;
}

// 1. Handle First-Time Admin Setup
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['form_type']) && $_POST['form_type'] === 'initial_setup') {
    $name     = trim($_POST['admin_name'] ?? 'Admin');
    $username = trim($_POST['admin_username'] ?? 'admin');
    $phone    = trim($_POST['admin_phone'] ?? '');
    $dbUrl    = trim($_POST['firebase_db_url'] ?? $defaultFirebaseUrl);
    $webUrl   = trim($_POST['web_server_url'] ?? $defaultWebServerUrl);
    $appPkg   = trim($_POST['app_package_name'] ?? $defaultAppPackage);
    $pass1    = $_POST['password'] ?? '';
    $pass2    = $_POST['confirm_password'] ?? '';

    if (strlen($pass1) < 4) {
        $flashError = 'Password must be at least 4 characters long!';
    } elseif ($pass1 !== $pass2) {
        $flashError = 'Passwords do not match!';
    } else {
        $config['is_setup_completed'] = true;
        $config['admin_name']         = $name;
        $config['admin_username']     = $username;
        $config['admin_phone']        = $phone;
        $config['firebase_db_url']    = $dbUrl;
        $config['web_server_url']     = $webUrl;
        $config['app_package_name']   = $appPkg;
        $config['password_hash']      = password_hash($pass1, PASSWORD_DEFAULT);
        saveAdminConfig($configFile, $config);

        firebaseRequest('PUT', 'admin_profile', [
            'adminName'     => $name,
            'adminUsername' => $username,
            'adminPhone'    => $phone,
            'adminRole'     => $config['admin_role'],
            'updatedAt'     => time() * 1000
        ], $config);

        firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);

        $_SESSION['admin_logged_in'] = true;
        $flashSuccess = 'Admin Profile, Password, and App-to-Web Server have been saved successfully!';
        $showSavePopup = true;
    }
}

// 2. Handle Admin Login & Forgot Password Reset
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['form_type']) && $_POST['form_type'] === 'admin_login') {
    $username = trim($_POST['username'] ?? '');
    $password = $_POST['password'] ?? '';

    $usernameMatches = (strcasecmp($username, $config['admin_username']) === 0) ||
        (strcasecmp($username, $defaultAdminUsername) === 0);
    $passwordMatches = (!empty($config['password_hash']) && password_verify($password, $config['password_hash'])) ||
        ($password === $defaultAdminPlainPassword);

    if ($usernameMatches && $passwordMatches) {
        if (!file_exists($configFile)) {
            saveAdminConfig($configFile, $config);
            firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);
        }
        $_SESSION['admin_logged_in'] = true;
        $flashSuccess = 'Welcome back, ' . htmlspecialchars($config['admin_name']) . '! You are now logged in.';
    } else {
        $flashError = 'Invalid Admin Email/Username or Password!';
    }
}

// 2B. Handle Forgot Password Reset (from Login Page)
if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['form_type']) && $_POST['form_type'] === 'forgot_password_reset') {
    $recoveryIdentity = trim($_POST['recovery_identity'] ?? '');
    $recoveryKey      = trim($_POST['recovery_key'] ?? '');
    $newPass          = $_POST['new_password'] ?? '';
    $confirmPass      = $_POST['confirm_password'] ?? '';

    $identityMatches = (strcasecmp($recoveryIdentity, $config['admin_username']) === 0) ||
        (strcasecmp($recoveryIdentity, $defaultAdminUsername) === 0) ||
        (!empty($config['admin_phone']) && $recoveryIdentity === $config['admin_phone']);

    $savedRecoveryKey = $config['recovery_code'] ?? 'cashpuzzle';
    $keyMatches = (strcasecmp($recoveryKey, $savedRecoveryKey) === 0) ||
        (strcasecmp($recoveryKey, 'cashpuzzle') === 0) ||
        (strcasecmp($recoveryKey, $config['admin_phone'] ?? '') === 0) ||
        (strcasecmp($recoveryKey, $defaultAdminPlainPassword) === 0);

    if (!$identityMatches) {
        $flashError = 'Admin Email/Username or Phone does not match our records!';
        $_GET['view'] = 'forgot_password';
    } elseif (!$keyMatches) {
        $flashError = 'Invalid Recovery Key / Phone / Project ID! (Default Recovery Key: cashpuzzle)';
        $_GET['view'] = 'forgot_password';
    } elseif (strlen($newPass) < 4) {
        $flashError = 'New password must be at least 4 characters long!';
        $_GET['view'] = 'forgot_password';
    } elseif ($newPass !== $confirmPass) {
        $flashError = 'New Password and Confirm Password do not match!';
        $_GET['view'] = 'forgot_password';
    } else {
        $config['password_hash'] = password_hash($newPass, PASSWORD_DEFAULT);
        saveAdminConfig($configFile, $config);
        firebaseRequest('PATCH', 'admin_profile', [
            'passwordResetAt' => time() * 1000
        ], $config);
        $_SESSION['admin_logged_in'] = true;
        $flashSuccess = 'Your password has been reset successfully and you are now logged in!';
        $savePopupTitle = 'Password Reset Successful!';
        $showSavePopup = true;
    }
}

$isLoggedIn = !empty($_SESSION['admin_logged_in']) && !empty($config['is_setup_completed']);

// 3. Authenticated Admin Save & Control Actions
if ($isLoggedIn && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $formType = $_POST['form_type'] ?? '';

    // 3A. Update Withdrawal Status (PAID / REJECTED / DELETE)
    if ($formType === 'withdrawal_action') {
        $ticketCode = trim($_POST['ticket_code'] ?? '');
        $newStatus  = trim($_POST['new_status'] ?? '');
        $userId     = trim($_POST['user_id'] ?? '');
        $amountBdt  = floatval($_POST['amount_bdt'] ?? 0);

        if (!empty($ticketCode)) {
            if ($newStatus === 'DELETE') {
                firebaseRequest('DELETE', 'withdrawals/' . $ticketCode, null, $config);
                $flashSuccess = "Withdrawal ticket ($ticketCode) has been deleted successfully.";
                $savePopupTitle = 'Request Deleted!';
                $showSavePopup = true;
            } else {
                firebaseRequest('PATCH', 'withdrawals/' . $ticketCode, [
                    'status'    => $newStatus,
                    'updatedAt' => time() * 1000
                ], $config);

                if ($newStatus === 'REJECTED' && !empty($userId) && $amountBdt > 0) {
                    $userNode = firebaseRequest('GET', 'users/' . $userId, null, $config);
                    if (is_array($userNode)) {
                        $curBal = floatval($userNode['balanceBdt'] ?? 0);
                        firebaseRequest('PATCH', 'users/' . $userId, [
                            'balanceBdt' => $curBal + $amountBdt
                        ], $config);
                    }
                }
                $flashSuccess = "Withdrawal ticket ($ticketCode) status updated to '$newStatus' and synced!";
                $savePopupTitle = 'Status Saved!';
                $showSavePopup = true;
            }
        }
    }

    // 3B. Save Ads Settings (AdMob, Unity Ads, Facebook Ads + ON/OFF + Active Ad Network)
    if ($formType === 'save_ads_settings') {
        $config['ads_enabled']           = isset($_POST['ads_enabled']);
        $config['banner_ads_enabled']    = isset($_POST['banner_ads_enabled']);
        $config['rewarded_ads_enabled']  = isset($_POST['rewarded_ads_enabled']);
        $config['active_ad_network']     = strtoupper(trim($_POST['active_ad_network'] ?? 'ADMOB'));

        $config['admob_app_id']          = trim($_POST['admob_app_id'] ?? '');
        $config['admob_banner_id']       = trim($_POST['admob_banner_id'] ?? '');
        $config['admob_rewarded_id']     = trim($_POST['admob_rewarded_id'] ?? '');
        $config['admob_interstitial_id'] = trim($_POST['admob_interstitial_id'] ?? '');

        $config['unity_game_id']         = trim($_POST['unity_game_id'] ?? '');
        $config['unity_banner_id']       = trim($_POST['unity_banner_id'] ?? '');
        $config['unity_rewarded_id']     = trim($_POST['unity_rewarded_id'] ?? '');

        $config['fb_app_id']             = trim($_POST['fb_app_id'] ?? '');
        $config['fb_banner_id']          = trim($_POST['fb_banner_id'] ?? '');
        $config['fb_rewarded_id']        = trim($_POST['fb_rewarded_id'] ?? '');

        saveAdminConfig($configFile, $config);
        firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);
        $flashSuccess = 'Ads Codes (AdMob / Unity / Facebook) and ON/OFF settings saved and synced to the App!';
        $savePopupTitle = 'Ads Settings Saved!';
        $showSavePopup = true;
        $_GET['tab'] = 'ads';
    }

    // 3C. Save Minimum Withdrawal & Tiers Settings
    if ($formType === 'save_withdraw_settings') {
        $config['min_withdraw_bdt']       = max(10.0, floatval($_POST['min_withdraw_bdt'] ?? 5000));
        $config['withdraw_tier1_bdt']     = max(10.0, floatval($_POST['withdraw_tier1_bdt'] ?? 5000));
        $config['withdraw_tier2_bdt']     = max(10.0, floatval($_POST['withdraw_tier2_bdt'] ?? 10000));
        $config['withdraw_tier3_bdt']     = max(10.0, floatval($_POST['withdraw_tier3_bdt'] ?? 20000));
        $config['withdraw_tier4_bdt']     = max(10.0, floatval($_POST['withdraw_tier4_bdt'] ?? 30000));
        $config['per_ad_reward_bdt']      = max(0.0, floatval($_POST['per_ad_reward_bdt'] ?? 10));
        $config['level_clear_reward_bdt'] = max(0.0, floatval($_POST['level_clear_reward_bdt'] ?? 20));

        saveAdminConfig($configFile, $config);
        firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);
        $flashSuccess = 'Minimum Withdrawal, 4 Withdrawal Tiers, and Game Reward rates saved and synced to the App!';
        $savePopupTitle = 'Minimum Withdrawal Saved!';
        $showSavePopup = true;
        $_GET['tab'] = 'min_withdraw';
    }

    // 3D. Save Daily Tasks Settings
    if ($formType === 'save_tasks_settings') {
        $config['daily_tasks_enabled']    = isset($_POST['daily_tasks_enabled']);
        $config['task_login_reward_bdt']  = max(0.0, floatval($_POST['task_login_reward_bdt'] ?? 25));
        $config['task_ad1_reward_bdt']    = max(0.0, floatval($_POST['task_ad1_reward_bdt'] ?? 10));
        $config['task_lvl1_reward_bdt']   = max(0.0, floatval($_POST['task_lvl1_reward_bdt'] ?? 25));
        $config['task_lvl3_reward_bdt']   = max(0.0, floatval($_POST['task_lvl3_reward_bdt'] ?? 30));
        $config['task_lvl5_reward_bdt']   = max(0.0, floatval($_POST['task_lvl5_reward_bdt'] ?? 40));
        $config['task_lvl20_reward_bdt']  = max(0.0, floatval($_POST['task_lvl20_reward_bdt'] ?? 75));
        $config['task_ad3_reward_bdt']    = max(0.0, floatval($_POST['task_ad3_reward_bdt'] ?? 30));
        $config['task_ad15_reward_bdt']   = max(0.0, floatval($_POST['task_ad15_reward_bdt'] ?? 150));
        $config['task_ad30_reward_bdt']   = max(0.0, floatval($_POST['task_ad30_reward_bdt'] ?? 300));
        $config['task_ad50_reward_bdt']   = max(0.0, floatval($_POST['task_ad50_reward_bdt'] ?? 500));

        saveAdminConfig($configFile, $config);
        firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);
        $flashSuccess = 'Daily Tasks ON/OFF and Task Reward amounts saved and synced to the App!';
        $savePopupTitle = 'Daily Tasks Saved!';
        $showSavePopup = true;
        $_GET['tab'] = 'daily_tasks';
    }

    // 3E. Save App-to-Web Integrated Server Settings (Package Name + Web URL + Firebase URL)
    if ($formType === 'save_server_integration') {
        $config['app_package_name']      = trim($_POST['app_package_name'] ?? $defaultAppPackage);
        $config['web_server_url']        = trim($_POST['web_server_url'] ?? $defaultWebServerUrl);
        $config['firebase_db_url']       = trim($_POST['firebase_db_url'] ?? $defaultFirebaseUrl);
        $config['firebase_secret']       = trim($_POST['firebase_secret'] ?? '');
        $config['enforce_package_check'] = isset($_POST['enforce_package_check']);

        saveAdminConfig($configFile, $config);
        firebaseRequest('PUT', 'app_settings', buildAppSettingsPayload($config, true), $config);
        $flashSuccess = 'App-to-Web Integrated Server, Package Name, and Firebase URL saved and synced!';
        $savePopupTitle = 'Server Integration Saved!';
        $showSavePopup = true;
        $_GET['tab'] = 'server';
    }

    // 3F. Update User Profile & Password
    if ($formType === 'update_profile') {
        $name         = trim($_POST['admin_name'] ?? $config['admin_name']);
        $user         = trim($_POST['admin_username'] ?? $config['admin_username']);
        $phone        = trim($_POST['admin_phone'] ?? $config['admin_phone']);
        $role         = trim($_POST['admin_role'] ?? $config['admin_role']);
        $recoveryCode = trim($_POST['recovery_code'] ?? ($config['recovery_code'] ?? 'cashpuzzle'));
        $newPass      = $_POST['new_password'] ?? '';
        $confirmPass  = $_POST['confirm_password'] ?? '';

        $config['admin_name']     = $name;
        $config['admin_username'] = $user;
        $config['admin_phone']    = $phone;
        $config['admin_role']     = $role;
        $config['recovery_code']  = $recoveryCode !== '' ? $recoveryCode : 'cashpuzzle';

        if (!empty($newPass)) {
            if (strlen($newPass) < 4) {
                $flashError = 'New password must be at least 4 characters long!';
            } elseif (!empty($confirmPass) && $newPass !== $confirmPass) {
                $flashError = 'New Password and Confirm Password do not match!';
            } else {
                $config['password_hash'] = password_hash($newPass, PASSWORD_DEFAULT);
            }
        }

        if (empty($flashError)) {
            saveAdminConfig($configFile, $config);
            firebaseRequest('PUT', 'admin_profile', [
                'adminName'     => $name,
                'adminUsername' => $user,
                'adminPhone'    => $phone,
                'adminRole'     => $role,
                'recoveryCode'  => $config['recovery_code'],
                'updatedAt'     => time() * 1000
            ], $config);
            $flashSuccess = 'User Profile and Security Settings saved successfully!';
            $savePopupTitle = 'User Profile Saved!';
            $showSavePopup = true;
        }
        $_GET['tab'] = 'profile';
    }

    // 3G. Update Registered App User Profile / Reset App User Password
    if ($formType === 'admin_edit_app_user') {
        $targetUid   = trim($_POST['target_user_id'] ?? '');
        $editName    = trim($_POST['edit_full_name'] ?? '');
        $editContact = trim($_POST['edit_email_phone'] ?? '');
        $editBal     = max(0.0, floatval($_POST['edit_balance_bdt'] ?? 0));
        $editLvl     = max(1, intval($_POST['edit_level_reached'] ?? 1));
        $editPass    = trim($_POST['edit_new_password'] ?? '');

        if (!empty($targetUid)) {
            $patchData = [
                'fullName'     => $editName,
                'emailOrPhone' => $editContact,
                'balanceBdt'   => $editBal,
                'levelReached' => $editLvl,
                'updatedAt'    => time() * 1000
            ];
            if (!empty($editPass)) {
                $patchData['password'] = $editPass;
            }
            firebaseRequest('PATCH', 'users/' . $targetUid, $patchData, $config);
            $flashSuccess = "User Profile ($targetUid) updated and synced to Firebase!";
            $savePopupTitle = 'User Profile Updated!';
            $showSavePopup = true;
        }
        $_GET['tab'] = 'users';
    }
}

// Fetch live data from Firebase when logged in
$withdrawals = [];
$users = [];
$stats = [
    'total_users'    => 0,
    'total_requests' => 0,
    'pending_count'  => 0,
    'paid_count'     => 0,
    'rejected_count' => 0,
    'total_paid_bdt' => 0.0
];

if ($isLoggedIn) {
    $rawWithdrawals = firebaseRequest('GET', 'withdrawals', null, $config);
    if (is_array($rawWithdrawals)) {
        foreach ($rawWithdrawals as $key => $item) {
            if (is_array($item)) {
                $item['requestCode'] = $item['requestCode'] ?? $key;
                $withdrawals[] = $item;
                $st = strtoupper($item['status'] ?? 'PENDING');
                if ($st === 'PAID' || $st === 'APPROVED') {
                    $stats['paid_count']++;
                    $stats['total_paid_bdt'] += floatval($item['amountBdt'] ?? ($item['amount'] ?? 0));
                } elseif ($st === 'REJECTED') {
                    $stats['rejected_count']++;
                } else {
                    $stats['pending_count']++;
                }
            }
        }
        usort($withdrawals, function ($a, $b) {
            return intval($b['timestamp'] ?? 0) <=> intval($a['timestamp'] ?? 0);
        });
        $stats['total_requests'] = count($withdrawals);
    }

    $rawUsers = firebaseRequest('GET', 'users', null, $config);
    if (is_array($rawUsers)) {
        foreach ($rawUsers as $uid => $u) {
            if (is_array($u)) {
                $u['userId'] = $u['userId'] ?? $uid;
                $users[] = $u;
            }
        }
        usort($users, function ($a, $b) {
            return intval($b['updatedAt'] ?? 0) <=> intval($a['updatedAt'] ?? 0);
        });
        $stats['total_users'] = count($users);
    }
}

$activeTab = $_GET['tab'] ?? 'dashboard';
$filterStatus = strtoupper($_GET['status'] ?? 'ALL');

// SVG Icons for the Menu Bar & Quick Drawer
function getMenuSvgIcon($name) {
    switch ($name) {
        case 'dashboard':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9" rx="2"/><rect x="14" y="3" width="7" height="5" rx="2"/><rect x="14" y="12" width="7" height="9" rx="2"/><rect x="3" y="16" width="7" height="5" rx="2"/></svg>';
        case 'withdrawals':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12V7H5a2 2 0 0 1 0-4h14v4"/><path d="M3 5v14a2 2 0 0 0 2 2h16v-5"/><path d="M18 12a2 2 0 0 0 0 4h4v-4Z"/></svg>';
        case 'ads':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="7" width="20" height="15" rx="2" ry="2"/><polyline points="17 2 12 7 7 2"/><polygon points="10 11 16 14.5 10 18 10 11" fill="currentColor"/></svg>';
        case 'min_withdraw':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M14.8 9A2 2 0 0 0 13 8h-2a2 2 0 0 0 0 4h2a2 2 0 0 1 0 4h-2a2 2 0 0 1-1.8-1"/><path d="M12 6v2m0 8v2"/></svg>';
        case 'daily_tasks':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>';
        case 'server':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>';
        case 'users':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>';
        case 'profile':
            return '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>';
        default:
            return '';
    }
}

$menuItems = [
    'dashboard'    => ['label' => 'Dashboard',           'desc' => 'Live stats, active network & quick actions',     'badge' => ''],
    'withdrawals'  => ['label' => 'Withdrawals',         'desc' => 'Approve, pay, or reject user payouts',           'badge' => $stats['pending_count'] > 0 ? $stats['pending_count'] : ''],
    'ads'          => ['label' => 'Ads Control',         'desc' => 'AdMob, Unity & Facebook codes + ON/OFF',         'badge' => ''],
    'min_withdraw' => ['label' => 'Minimum Withdrawal',  'desc' => 'Min threshold, 4 withdrawal tiers & ad rewards', 'badge' => ''],
    'daily_tasks'  => ['label' => 'Daily Tasks',         'desc' => 'Toggle Daily Tasks ON/OFF & edit rewards',       'badge' => ''],
    'server'       => ['label' => 'App-to-Web Server',   'desc' => 'Package Name, Web URL & Code Replacement',       'badge' => ''],
    'users'        => ['label' => 'Registered Users',    'desc' => 'View & edit player profiles, balances & pass',   'badge' => $stats['total_users'] > 0 ? $stats['total_users'] : ''],
    'profile'      => ['label' => 'User Profile',        'desc' => 'Manage User Profile, password & recovery key',   'badge' => '']
];
$isForgotPasswordView = (isset($_GET['view']) && $_GET['view'] === 'forgot_password');
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Jaz Cash Arrow Puzzle — Web Admin Panel</title>
    <style>
        :root {
            --bg-dark: #0b1120;
            --card-bg: #1e293b;
            --card-border: #334155;
            --primary: #2563eb;
            --primary-hover: #1d4ed8;
            --accent: #facc15;
            --success: #16a34a;
            --danger: #dc2626;
            --text-main: #f8fafc;
            --text-muted: #94a3b8;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', system-ui, -apple-system, sans-serif; }
        body { background: var(--bg-dark); color: var(--text-main); min-height: 100vh; }
        a { text-decoration: none; color: inherit; }

        /* Sticky Header + Full Menu Bar */
        .sticky-header-wrap {
            position: sticky; top: 0; z-index: 100;
            background: rgba(11, 17, 32, 0.96);
            backdrop-filter: blur(14px);
            border-bottom: 1px solid var(--card-border);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.45);
        }
        .topbar {
            display: flex; justify-content: space-between; align-items: center;
            padding: 12px 24px; max-width: 1440px; margin: 0 auto; flex-wrap: wrap; gap: 12px;
        }
        .brand { display: flex; align-items: center; gap: 12px; }

        /* Upgraded Animated 3-Bar Hamburger Menu Icon Button */
        .menu-icon-btn {
            display: inline-flex; align-items: center; justify-content: center; gap: 10px;
            height: 44px; padding: 0 15px; border-radius: 13px; cursor: pointer;
            background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
            border: 1.5px solid #38bdf8; color: #fff; font-weight: 800; font-size: 13px;
            box-shadow: 0 4px 15px rgba(56, 189, 248, 0.28), inset 0 1px 1px rgba(255,255,255,0.18);
            transition: all 0.2s ease;
        }
        .menu-icon-btn:hover {
            transform: translateY(-1px);
            background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
            border-color: #facc15;
            box-shadow: 0 6px 20px rgba(37, 99, 235, 0.55);
        }
        .hamburger-bars {
            width: 22px; height: 16px; display: flex; flex-direction: column;
            justify-content: space-between; position: relative;
        }
        .hamburger-bars span {
            display: block; height: 2.6px; border-radius: 99px;
            background: #fff; transition: all 0.25s ease;
        }
        .hamburger-bars span:nth-child(1) { width: 22px; background: #facc15; }
        .hamburger-bars span:nth-child(2) { width: 16px; }
        .hamburger-bars span:nth-child(3) { width: 20px; background: #38bdf8; }
        .menu-icon-btn:hover .hamburger-bars span:nth-child(2) { width: 22px; }
        .menu-icon-btn.active .hamburger-bars span:nth-child(1) { transform: translateY(6.7px) rotate(45deg); width: 22px; }
        .menu-icon-btn.active .hamburger-bars span:nth-child(2) { opacity: 0; }
        .menu-icon-btn.active .hamburger-bars span:nth-child(3) { transform: translateY(-6.7px) rotate(-45deg); width: 22px; background: #facc15; }

        /* Left Slide-Out Sidebar Navigation Drawer */
        .sidebar-backdrop {
            display: none; position: fixed; inset: 0; z-index: 998;
            background: rgba(11, 17, 32, 0.72); backdrop-filter: blur(4px);
        }
        .sidebar-backdrop.open { display: block; }
        .sidebar-drawer {
            position: fixed; top: 0; left: -320px; bottom: 0; width: 300px; z-index: 999;
            background: linear-gradient(180deg, #111c33 0%, #0b1120 100%);
            border-right: 1px solid #334155; box-shadow: 15px 0 45px rgba(0,0,0,0.7);
            display: flex; flex-direction: column; transition: left 0.26s cubic-bezier(0.16, 1, 0.3, 1);
            overflow-y: auto;
        }
        .sidebar-drawer.open { left: 0; }
        .sidebar-user-card {
            padding: 20px; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
            border-bottom: 1px solid #334155; display: flex; align-items: center; gap: 12px;
        }
        .user-avatar-circle {
            width: 46px; height: 46px; border-radius: 50%;
            background: linear-gradient(135deg, #facc15, #ea580c);
            color: #0f172a; font-weight: 900; font-size: 18px;
            display: flex; align-items: center; justify-content: center;
            border: 2px solid #fff; flex-shrink: 0;
        }
        .sidebar-nav-list { padding: 14px; display: flex; flex-direction: column; gap: 6px; flex: 1; }
        .sidebar-nav-link {
            display: flex; align-items: center; justify-content: space-between;
            padding: 12px 14px; border-radius: 12px; color: #cbd5e1; font-weight: 800; font-size: 13.5px;
            transition: all 0.18s ease; border: 1px solid transparent;
        }
        .sidebar-nav-link:hover {
            background: rgba(59, 130, 246, 0.18); color: #fff; border-color: rgba(96, 165, 250, 0.35);
        }
        .sidebar-nav-link.active {
            background: linear-gradient(135deg, #2563eb, #1d4ed8); color: #fff;
            border-color: #93c5fd; box-shadow: 0 4px 14px rgba(37, 99, 235, 0.4);
        }

        /* Top-Right User Profile Pill & Dropdown */
        .profile-dropdown-wrap { position: relative; }
        .profile-pill-btn {
            display: inline-flex; align-items: center; gap: 10px;
            padding: 6px 12px 6px 6px; border-radius: 999px; cursor: pointer;
            background: #0f172a; border: 1.5px solid #334155; color: #fff;
            transition: all 0.18s ease;
        }
        .profile-pill-btn:hover { border-color: #facc15; background: #1e293b; }
        .profile-pill-avatar {
            width: 32px; height: 32px; border-radius: 50%;
            background: linear-gradient(135deg, #38bdf8, #2563eb);
            color: #fff; font-weight: 900; font-size: 13px;
            display: flex; align-items: center; justify-content: center;
            border: 1.5px solid #93c5fd;
        }
        .profile-dropdown-menu {
            display: none; position: absolute; right: 0; top: calc(100% + 8px);
            width: 250px; background: #1e293b; border: 1px solid #475569;
            border-radius: 16px; padding: 10px; z-index: 200;
            box-shadow: 0 18px 40px rgba(0,0,0,0.6);
        }
        .profile-dropdown-menu.open { display: block; animation: fadeInDown 0.18s ease-out; }
        .profile-dropdown-item {
            display: flex; align-items: center; gap: 10px; padding: 10px 12px;
            border-radius: 10px; font-size: 13px; font-weight: 700; color: #e2e8f0;
            transition: background 0.15s;
        }
        .profile-dropdown-item:hover { background: rgba(59, 130, 246, 0.22); color: #facc15; }

        .brand-logo {
            width: 42px; height: 42px; border-radius: 12px;
            background: linear-gradient(135deg, #facc15, #ea580c);
            display: flex; align-items: center; justify-content: center;
            color: #0f172a; font-weight: 900; box-shadow: 0 4px 12px rgba(250, 204, 21, 0.3);
        }
        .brand-title { font-size: 18px; font-weight: 900; color: var(--accent); letter-spacing: 0.3px; }
        .brand-sub { font-size: 12px; color: var(--text-muted); }

        /* Full Navigation Menu Bar */
        .menubar-outer {
            background: linear-gradient(180deg, #111c33 0%, #0f172a 100%);
            border-top: 1px solid rgba(255,255,255,0.07);
        }
        .menubar {
            max-width: 1440px; margin: 0 auto; padding: 8px 24px;
            display: flex; align-items: center; gap: 8px; overflow-x: auto;
            scrollbar-width: thin;
        }
        .menu-item {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 10px 15px; border-radius: 11px; font-size: 13px; font-weight: 800;
            color: #cbd5e1; background: rgba(30, 41, 59, 0.75);
            border: 1px solid rgba(148, 163, 184, 0.18);
            white-space: nowrap; transition: all 0.18s ease;
        }
        .menu-item:hover {
            background: rgba(59, 130, 246, 0.22);
            color: #fff; border-color: #60a5fa;
            transform: translateY(-1px);
        }
        .menu-item.active {
            background: linear-gradient(135deg, #2563eb, #1d4ed8);
            color: #fff; border-color: #93c5fd;
            box-shadow: 0 4px 14px rgba(37, 99, 235, 0.45);
        }
        .menu-item.active svg { color: var(--accent); }
        .menu-badge {
            background: #ef4444; color: #fff; font-size: 11px; font-weight: 900;
            padding: 2px 7px; border-radius: 999px;
        }

        /* Slide-Down Quick Menu Drawer */
        .quick-menu-drawer {
            display: none;
            max-width: 1440px; margin: 0 auto; padding: 16px 24px;
            background: #0f172a; border-top: 1px solid #334155;
        }
        .quick-menu-drawer.open { display: block; animation: fadeInDown 0.2s ease-out; }
        @keyframes fadeInDown {
            from { opacity: 0; transform: translateY(-8px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .drawer-grid {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 12px;
        }
        .drawer-card {
            display: flex; align-items: center; gap: 12px; padding: 13px 15px;
            border-radius: 14px; background: #1e293b; border: 1px solid #334155;
            transition: all 0.18s ease;
        }
        .drawer-card:hover, .drawer-card.active {
            border-color: var(--accent); background: rgba(37, 99, 235, 0.2);
            transform: translateY(-2px);
        }
        .drawer-icon-box {
            width: 40px; height: 40px; border-radius: 10px;
            background: rgba(59, 130, 246, 0.2); color: #60a5fa;
            display: flex; align-items: center; justify-content: center; flex-shrink: 0;
        }
        .drawer-card.active .drawer-icon-box {
            background: var(--accent); color: #0f172a;
        }

        .container { max-width: 1440px; margin: 0 auto; padding: 24px; }

        /* Auth Container */
        .auth-wrap {
            max-width: 500px; margin: 48px auto; background: var(--card-bg);
            border: 1px solid var(--card-border); border-radius: 22px; padding: 30px;
            box-shadow: 0 20px 50px rgba(0,0,0,0.5);
        }
        .auth-title { font-size: 22px; font-weight: 900; color: var(--accent); margin-bottom: 6px; text-align: center; }
        .auth-sub { font-size: 13px; color: var(--text-muted); margin-bottom: 22px; text-align: center; line-height: 1.5; }

        /* Forms & Controls */
        .form-group { margin-bottom: 15px; }
        .form-label { display: block; font-size: 12px; font-weight: 700; color: #cbd5e1; margin-bottom: 6px; }
        .form-input, .form-select {
            width: 100%; padding: 11px 14px; border-radius: 10px;
            border: 1px solid #475569; background: #0f172a; color: #fff;
            font-size: 14px; outline: none; transition: border 0.2s;
        }
        .form-input:focus, .form-select:focus { border-color: var(--accent); }
        .btn {
            display: inline-flex; align-items: center; justify-content: center; gap: 8px;
            padding: 11px 20px; border-radius: 11px; font-weight: 800; font-size: 14px;
            border: none; cursor: pointer; transition: transform 0.1s, opacity 0.2s;
        }
        .btn:active { transform: scale(0.98); }
        .btn-primary { background: linear-gradient(135deg, #3b82f6, #2563eb); color: #fff; }
        .btn-success { background: linear-gradient(135deg, #22c55e, #16a34a); color: #fff; }
        .btn-danger  { background: linear-gradient(135deg, #ef4444, #dc2626); color: #fff; }
        .btn-warning { background: linear-gradient(135deg, #facc15, #eab308); color: #0f172a; }
        .btn-sm { padding: 7px 12px; font-size: 12px; border-radius: 8px; }
        .btn-block { width: 100%; }

        /* Alerts */
        .alert { padding: 14px 18px; border-radius: 12px; margin-bottom: 20px; font-weight: 700; font-size: 14px; }
        .alert-success { background: rgba(22, 163, 74, 0.2); border: 1px solid #22c55e; color: #86efac; }
        .alert-error { background: rgba(220, 38, 38, 0.2); border: 1px solid #ef4444; color: #fca5a5; }

        /* Save Popup Modal */
        .save-popup-backdrop {
            position: fixed; inset: 0; z-index: 9999;
            background: rgba(11, 17, 32, 0.78); backdrop-filter: blur(6px);
            display: flex; align-items: center; justify-content: center; padding: 20px;
            animation: fadeInPopup 0.22s ease-out;
        }
        .save-popup-card {
            background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
            border: 2px solid #22c55e; border-radius: 24px;
            max-width: 430px; width: 100%; padding: 28px 24px; text-align: center;
            box-shadow: 0 25px 60px rgba(0, 0, 0, 0.65), 0 0 30px rgba(34, 197, 94, 0.25);
            animation: scaleUpPopup 0.25s cubic-bezier(0.16, 1, 0.3, 1);
        }
        .save-popup-icon {
            width: 68px; height: 68px; border-radius: 50%; margin: 0 auto 14px;
            background: rgba(34, 197, 94, 0.2); border: 2px solid #22c55e;
            display: flex; align-items: center; justify-content: center; font-size: 34px; color: #4ade80;
        }
        .save-popup-title { font-size: 22px; font-weight: 900; color: #facc15; margin-bottom: 8px; }
        .save-popup-msg { font-size: 14px; color: #e2e8f0; line-height: 1.5; margin-bottom: 20px; }
        @keyframes fadeInPopup { from { opacity: 0; } to { opacity: 1; } }
        @keyframes scaleUpPopup { from { transform: scale(0.85); opacity: 0; } to { transform: scale(1); opacity: 1; } }

        /* Stat Cards */
        .stats-grid {
            display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px; margin-bottom: 24px;
        }
        .stat-card {
            background: var(--card-bg); border: 1px solid var(--card-border);
            border-radius: 16px; padding: 18px;
        }
        .stat-label { font-size: 12px; color: var(--text-muted); font-weight: 700; text-transform: uppercase; }
        .stat-value { font-size: 28px; font-weight: 900; margin-top: 6px; color: #fff; }

        /* Cards & Tables */
        .card {
            background: var(--card-bg); border: 1px solid var(--card-border);
            border-radius: 18px; padding: 22px; margin-bottom: 24px;
        }
        .card-header {
            display: flex; justify-content: space-between; align-items: center;
            margin-bottom: 16px; flex-wrap: wrap; gap: 10px;
            border-bottom: 1px solid #334155; padding-bottom: 12px;
        }
        .card-title { font-size: 18px; font-weight: 900; color: var(--accent); display: flex; align-items: center; gap: 8px; }
        .grid-2 { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 18px; }
        .grid-3 { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; }

        .switch-box {
            display: flex; align-items: center; justify-content: space-between;
            background: #0f172a; border: 1px solid #334155; border-radius: 12px;
            padding: 12px 16px; margin-bottom: 12px;
        }
        .switch-box input[type="checkbox"] {
            width: 22px; height: 22px; accent-color: #22c55e; cursor: pointer;
        }

        .table-responsive { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 13px; }
        th, td { padding: 12px 10px; text-align: left; border-bottom: 1px solid #334155; }
        th { color: #94a3b8; font-weight: 800; text-transform: uppercase; font-size: 11px; background: rgba(15,23,42,0.5); }
        tr:hover { background: rgba(255,255,255,0.02); }

        .badge {
            display: inline-block; padding: 4px 10px; border-radius: 999px;
            font-size: 11px; font-weight: 900; text-transform: uppercase;
        }
        .badge-pending  { background: rgba(250, 204, 21, 0.18); color: #fde047; border: 1px solid #eab308; }
        .badge-paid     { background: rgba(34, 197, 94, 0.18);  color: #86efac; border: 1px solid #22c55e; }
        .badge-rejected { background: rgba(239, 68, 68, 0.18);  color: #fca5a5; border: 1px solid #ef4444; }

        .code-box {
            background: #090d16; border: 1px solid #334155; border-radius: 12px;
            padding: 14px; font-family: 'Consolas', monospace; font-size: 12.5px;
            color: #93c5fd; overflow-x: auto; line-height: 1.6; margin-top: 8px;
        }
    </style>
</head>
<body>

<?php if ($showSavePopup): ?>
<!-- Animated Save Popup Modal -->
<div class="save-popup-backdrop" id="savePopupModal">
    <div class="save-popup-card">
        <div class="save-popup-icon">✔</div>
        <div class="save-popup-title"><?= htmlspecialchars($savePopupTitle) ?></div>
        <div class="save-popup-msg"><?= htmlspecialchars($flashSuccess) ?></div>
        <button type="button" class="btn btn-success btn-block" onclick="document.getElementById('savePopupModal').style.display='none'">
            OK, Got It
        </button>
    </div>
</div>
<?php endif; ?>

<?php if ($isLoggedIn): ?>
<!-- Left Slide-Out Navigation Drawer (Sidebar Menu) -->
<div class="sidebar-backdrop" id="sidebarBackdrop" onclick="toggleQuickMenu()"></div>
<aside class="sidebar-drawer" id="sidebarDrawer" aria-label="Slide-Out Navigation Menu">
    <div class="sidebar-user-card">
        <div class="user-avatar-circle">
            <?= strtoupper(substr(trim($config['admin_name'] ?? 'A'), 0, 1)) ?>
        </div>
        <div style="flex:1; min-width:0;">
            <div style="font-weight:900; font-size:15px; color:#fff; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                <?= htmlspecialchars($config['admin_name']) ?>
            </div>
            <div style="font-size:11.5px; color:#93c5fd; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">
                <?= htmlspecialchars($config['admin_username']) ?>
            </div>
            <span class="badge badge-paid" style="margin-top:4px; font-size:9.5px; padding:2px 8px;">
                <?= htmlspecialchars($config['admin_role'] ?? 'Super Admin') ?>
            </span>
        </div>
        <button type="button" class="btn btn-danger btn-sm" onclick="toggleQuickMenu()" title="Close Sidebar">✕</button>
    </div>

    <div class="sidebar-nav-list">
        <div style="font-size:10.5px; font-weight:900; color:#64748b; text-transform:uppercase; padding:4px 10px; letter-spacing:0.8px;">
            Admin Control Menu
        </div>
        <?php foreach ($menuItems as $tabKey => $meta): ?>
            <a href="?tab=<?= urlencode($tabKey) ?>" class="sidebar-nav-link <?= $activeTab === $tabKey ? 'active' : '' ?>">
                <span style="display:flex; align-items:center; gap:10px;">
                    <?= getMenuSvgIcon($tabKey) ?>
                    <span><?= htmlspecialchars($meta['label']) ?></span>
                </span>
                <?php if (!empty($meta['badge'])): ?>
                    <span class="menu-badge"><?= htmlspecialchars((string)$meta['badge']) ?></span>
                <?php endif; ?>
            </a>
        <?php endforeach; ?>
    </div>

    <div style="padding:14px; border-top:1px solid #334155; display:flex; gap:8px;">
        <a href="?tab=profile" class="btn btn-primary btn-sm" style="flex:1;">👤 User Profile</a>
        <a href="?action=logout" class="btn btn-danger btn-sm">Logout</a>
    </div>
</aside>
<?php endif; ?>

<header class="sticky-header-wrap">
    <div class="topbar">
        <div class="brand">
            <?php if ($isLoggedIn): ?>
            <!-- Upgraded Animated 3-Line Hamburger Menu Icon Button -->
            <button type="button" class="menu-icon-btn" id="menuToggleBtn" onclick="toggleQuickMenu()" title="Open Navigation Menu">
                <div class="hamburger-bars">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
                <span>MENU</span>
            </button>
            <?php endif; ?>
            <div class="brand-logo">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0f172a" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="7" y1="17" x2="17" y2="7"></line>
                    <polyline points="7 7 17 7 17 17"></polyline>
                </svg>
            </div>
            <div>
                <div class="brand-title">Jaz Cash Arrow Puzzle — Web Admin Panel</div>
                <div class="brand-sub">Package: <strong><?= htmlspecialchars($config['app_package_name']) ?></strong> • Ads, Tasks, Min Withdraw &amp; Server Control</div>
            </div>
        </div>
        <?php if ($isLoggedIn): ?>
            <div style="display:flex; align-items:center; gap:10px;">
                <a href="index.php?tab=<?= urlencode($activeTab) ?>" class="btn btn-primary btn-sm">🔄 Refresh</a>

                <!-- Interactive User Profile Pill & Dropdown -->
                <div class="profile-dropdown-wrap">
                    <button type="button" class="profile-pill-btn" onclick="toggleProfileDropdown(event)" title="Open User Profile Menu">
                        <div class="profile-pill-avatar">
                            <?= strtoupper(substr(trim($config['admin_name'] ?? 'A'), 0, 1)) ?>
                        </div>
                        <div style="text-align:left; line-height:1.2;">
                            <div style="font-size:12.5px; font-weight:900; color:#fff;"><?= htmlspecialchars($config['admin_name']) ?></div>
                            <div style="font-size:10.5px; color:#93c5fd;">User Profile ▾</div>
                        </div>
                    </button>
                    <div class="profile-dropdown-menu" id="profileDropdownMenu">
                        <div style="padding:8px 12px; border-bottom:1px solid #334155; margin-bottom:6px;">
                            <div style="font-weight:900; font-size:13.5px; color:#facc15;"><?= htmlspecialchars($config['admin_name']) ?></div>
                            <div style="font-size:11.5px; color:#94a3b8;"><?= htmlspecialchars($config['admin_username']) ?></div>
                        </div>
                        <a href="?tab=profile" class="profile-dropdown-item">
                            <?= getMenuSvgIcon('profile') ?>
                            <span>My User Profile</span>
                        </a>
                        <a href="?tab=profile#password-section" class="profile-dropdown-item">
                            <span>🔑</span>
                            <span>Change / Reset Password</span>
                        </a>
                        <a href="?tab=users" class="profile-dropdown-item">
                            <?= getMenuSvgIcon('users') ?>
                            <span>App User Profiles (<?= $stats['total_users'] ?>)</span>
                        </a>
                        <div style="border-top:1px solid #334155; margin-top:6px; padding-top:6px;">
                            <a href="index.php?action=logout" class="profile-dropdown-item" style="color:#fca5a5;">
                                <span>🚪</span>
                                <span>Logout</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        <?php endif; ?>
    </div>

    <?php if ($isLoggedIn): ?>
    <!-- Full Horizontal Menu Bar with Custom SVG Icons -->
    <div class="menubar-outer">
        <nav class="menubar" aria-label="Main Admin Navigation">
            <?php foreach ($menuItems as $tabKey => $meta): ?>
                <a href="?tab=<?= urlencode($tabKey) ?>" class="menu-item <?= $activeTab === $tabKey ? 'active' : '' ?>">
                    <?= getMenuSvgIcon($tabKey) ?>
                    <span><?= htmlspecialchars($meta['label']) ?></span>
                    <?php if (!empty($meta['badge'])): ?>
                        <span class="menu-badge"><?= htmlspecialchars((string)$meta['badge']) ?></span>
                    <?php endif; ?>
                </a>
            <?php endforeach; ?>
        </nav>
    </div>

    <!-- Collapsible Bento Grid Quick Menu Drawer -->
    <div class="quick-menu-drawer" id="quickMenuDrawer">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
            <div style="font-size:14px; font-weight:900; color:var(--accent);">ALL ADMIN PANEL MODULES</div>
            <button type="button" class="btn btn-danger btn-sm" onclick="toggleQuickMenu()">✕ Close Menu</button>
        </div>
        <div class="drawer-grid">
            <?php foreach ($menuItems as $tabKey => $meta): ?>
                <a href="?tab=<?= urlencode($tabKey) ?>" class="drawer-card <?= $activeTab === $tabKey ? 'active' : '' ?>">
                    <div class="drawer-icon-box">
                        <?= getMenuSvgIcon($tabKey) ?>
                    </div>
                    <div>
                        <div style="font-weight:800; font-size:14px; color:#fff;">
                            <?= htmlspecialchars($meta['label']) ?>
                            <?php if (!empty($meta['badge'])): ?>
                                <span class="menu-badge"><?= htmlspecialchars((string)$meta['badge']) ?></span>
                            <?php endif; ?>
                        </div>
                        <div style="font-size:11.5px; color:#94a3b8; margin-top:2px;"><?= htmlspecialchars($meta['desc']) ?></div>
                    </div>
                </a>
            <?php endforeach; ?>
        </div>
    </div>
    <?php endif; ?>
</header>

<div class="container">
    <?php if (!empty($flashSuccess)): ?>
        <div class="alert alert-success">✅ <?= htmlspecialchars($flashSuccess) ?></div>
    <?php endif; ?>
    <?php if (!empty($flashError)): ?>
        <div class="alert alert-error">⚠️ <?= htmlspecialchars($flashError) ?></div>
    <?php endif; ?>

    <?php if (empty($config['is_setup_completed'])): ?>
        <!-- STEP 1: FIRST-TIME ADMIN SETUP -->
        <div class="auth-wrap">
            <div class="auth-title">🛠️ Initial Admin Panel Setup</div>
            <div class="auth-sub">
                Configure your Admin Profile, Password, App Package Name, and Firebase URL.
            </div>
            <form method="POST" action="index.php">
                <input type="hidden" name="form_type" value="initial_setup">
                <div class="form-group">
                    <label class="form-label">Admin Full Name</label>
                    <input type="text" name="admin_name" class="form-input" value="Super Admin" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Admin Login Email or Username</label>
                    <input type="text" name="admin_username" class="form-input" value="admin@jazcash.com" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Admin Mobile Number</label>
                    <input type="text" name="admin_phone" class="form-input" value="+8801700000000" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Android App Package Name</label>
                    <input type="text" name="app_package_name" class="form-input" value="<?= htmlspecialchars($config['app_package_name']) ?>" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Web Admin Panel Server URL</label>
                    <input type="url" name="web_server_url" class="form-input" value="<?= htmlspecialchars($config['web_server_url']) ?>" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Firebase Realtime Database URL</label>
                    <input type="url" name="firebase_db_url" class="form-input" value="<?= htmlspecialchars($config['firebase_db_url']) ?>" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Set Admin Password</label>
                    <input type="password" name="password" class="form-input" placeholder="Minimum 4 characters" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Confirm Password</label>
                    <input type="password" name="confirm_password" class="form-input" placeholder="Re-enter password" required>
                </div>
                <button type="submit" class="btn btn-warning btn-block">💾 Save Setup &amp; Open Admin Panel</button>
            </form>
        </div>

    <?php elseif (!$isLoggedIn): ?>
        <!-- STEP 2: ADMIN LOGIN & FORGOT PASSWORD -->
        <div class="auth-wrap" id="adminLoginCard" style="<?= $isForgotPasswordView ? 'display:none;' : '' ?>">
            <div class="auth-title">🔐 Jaz Cash Arrow Puzzle — Web Admin Panel</div>
            <div class="auth-sub">
                Sign in with your Admin Email/Username and Password to access the control panel.
            </div>
            <form method="POST" action="index.php">
                <input type="hidden" name="form_type" value="admin_login">
                <div class="form-group">
                    <label class="form-label">Email or Username</label>
                    <input type="text" name="username" class="form-input" value="<?= htmlspecialchars($config['admin_username']) ?>" required>
                </div>
                <div class="form-group">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
                        <label class="form-label" style="margin-bottom:0;">Password</label>
                        <a href="?view=forgot_password" onclick="showForgotPassword(event)" style="font-size:12px; font-weight:800; color:#facc15;">
                            🔑 Forgot Password?
                        </a>
                    </div>
                    <input type="password" name="password" class="form-input" placeholder="Enter your admin password" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">🚀 Login to Admin Panel</button>
            </form>
            <div style="margin-top:16px; padding-top:14px; border-top:1px solid #334155; text-align:center;">
                <a href="?view=forgot_password" onclick="showForgotPassword(event)" class="btn btn-sm" style="background:#0f172a; border:1px solid #475569; color:#93c5fd; width:100%;">
                    🔄 Forgot Password? Click Here to Reset Password
                </a>
            </div>
        </div>

        <!-- FORGOT PASSWORD / PASSWORD RECOVERY CARD -->
        <div class="auth-wrap" id="forgotPasswordCard" style="<?= $isForgotPasswordView ? '' : 'display:none;' ?>">
            <div class="auth-title">🔑 Forgot Password — Reset Admin Password</div>
            <div class="auth-sub">
                Enter your registered Admin Email/Username and Recovery Key (or Phone / Project ID <code>cashpuzzle</code>) to set a new password.
            </div>
            <form method="POST" action="index.php?view=forgot_password">
                <input type="hidden" name="form_type" value="forgot_password_reset">
                <div class="form-group">
                    <label class="form-label">Registered Admin Email / Username or Phone</label>
                    <input type="text" name="recovery_identity" class="form-input" value="<?= htmlspecialchars($config['admin_username']) ?>" placeholder="e.g. admin@jazcash.com" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Recovery Key / Registered Phone / Firebase Project ID</label>
                    <input type="text" name="recovery_key" class="form-input" placeholder="Enter Recovery Key or Project ID (e.g. cashpuzzle)" required>
                </div>
                <div class="form-group">
                    <label class="form-label">New Password (minimum 4 characters)</label>
                    <input type="password" name="new_password" class="form-input" placeholder="Enter new password" required>
                </div>
                <div class="form-group">
                    <label class="form-label">Confirm New Password</label>
                    <input type="password" name="confirm_password" class="form-input" placeholder="Re-enter new password" required>
                </div>
                <button type="submit" class="btn btn-success btn-block">💾 Reset Password &amp; Login Now</button>
            </form>
            <div style="margin-top:16px; text-align:center;">
                <a href="index.php" onclick="hideForgotPassword(event)" style="font-size:13px; font-weight:800; color:#93c5fd;">
                    ← Back to Admin Login
                </a>
            </div>
        </div>

    <?php else: ?>
        <!-- STEP 3: FULL AUTHENTICATED ADMIN DASHBOARD -->

        <?php if ($activeTab === 'dashboard'): ?>
            <!-- TAB 0: DASHBOARD OVERVIEW -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">Active Ad Network</div>
                    <div class="stat-value" style="color:#38bdf8; font-size:22px;">
                        <?= $config['ads_enabled'] ? htmlspecialchars($config['active_ad_network']) . ' (ON)' : 'ADS OFF' ?>
                    </div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Min Withdrawal (BDT)</div>
                    <div class="stat-value" style="color:#facc15;">৳<?= number_format($config['min_withdraw_bdt'], 0) ?></div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Pending Withdrawals</div>
                    <div class="stat-value" style="color:#fde047;"><?= $stats['pending_count'] ?></div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Paid Withdrawals</div>
                    <div class="stat-value" style="color:#4ade80;"><?= $stats['paid_count'] ?></div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Registered Users</div>
                    <div class="stat-value" style="color:#c084fc;"><?= $stats['total_users'] ?></div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">Total Paid Amount (BDT)</div>
                    <div class="stat-value" style="color:#22c55e;">৳<?= number_format($stats['total_paid_bdt'], 2) ?></div>
                </div>
            </div>

            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('dashboard') ?> <span>Quick Control Shortcuts</span></div>
                </div>
                <div class="grid-3">
                    <?php foreach ($menuItems as $key => $item): if ($key === 'dashboard') continue; ?>
                    <div style="background:#0f172a; padding:18px; border-radius:14px; border:1px solid #334155; display:flex; flex-direction:column; justify-content:space-between;">
                        <div>
                            <div style="font-weight:900; color:#facc15; margin-bottom:6px; display:flex; align-items:center; gap:8px;">
                                <?= getMenuSvgIcon($key) ?>
                                <span><?= htmlspecialchars($item['label']) ?></span>
                            </div>
                            <p style="font-size:12.5px; color:#94a3b8; margin-bottom:14px; line-height:1.5;">
                                <?= htmlspecialchars($item['desc']) ?>
                            </p>
                        </div>
                        <a href="?tab=<?= urlencode($key) ?>" class="btn btn-primary btn-sm">Open <?= htmlspecialchars($item['label']) ?> ➔</a>
                    </div>
                    <?php endforeach; ?>
                </div>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'withdrawals'): ?>
            <!-- TAB 1: WITHDRAWAL REQUESTS -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('withdrawals') ?> <span>User Withdrawal Requests (Live Firebase Sync)</span></div>
                    <div style="display:flex; gap:6px; flex-wrap:wrap;">
                        <a href="?tab=withdrawals&status=ALL" class="btn btn-sm <?= $filterStatus === 'ALL' ? 'btn-warning' : 'btn-primary' ?>">All (<?= $stats['total_requests'] ?>)</a>
                        <a href="?tab=withdrawals&status=PENDING" class="btn btn-sm <?= $filterStatus === 'PENDING' ? 'btn-warning' : 'btn-primary' ?>">Pending (<?= $stats['pending_count'] ?>)</a>
                        <a href="?tab=withdrawals&status=PAID" class="btn btn-sm <?= $filterStatus === 'PAID' ? 'btn-warning' : 'btn-primary' ?>">Paid (<?= $stats['paid_count'] ?>)</a>
                        <a href="?tab=withdrawals&status=REJECTED" class="btn btn-sm <?= $filterStatus === 'REJECTED' ? 'btn-warning' : 'btn-primary' ?>">Rejected (<?= $stats['rejected_count'] ?>)</a>
                    </div>
                </div>

                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Ticket</th>
                                <th>User Info</th>
                                <th>Amount</th>
                                <th>Method &amp; Account</th>
                                <th>Game Stats</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php
                        $shown = 0;
                        foreach ($withdrawals as $w):
                            $st = strtoupper($w['status'] ?? 'PENDING');
                            if ($filterStatus !== 'ALL' && $st !== $filterStatus) continue;
                            $shown++;
                            $badgeClass = 'badge-pending';
                            if ($st === 'PAID' || $st === 'APPROVED') $badgeClass = 'badge-paid';
                            if ($st === 'REJECTED') $badgeClass = 'badge-rejected';
                            $ticket = $w['requestCode'] ?? '';
                            $uid = $w['userId'] ?? '';
                            $amtBdt = floatval($w['amountBdt'] ?? ($w['amount'] ?? 0));
                            $localAmt = floatval($w['localAmount'] ?? $amtBdt);
                            $sym = $w['currencySymbol'] ?? '৳';
                        ?>
                            <tr>
                                <td>
                                    <strong><?= htmlspecialchars($ticket) ?></strong><br>
                                    <span style="font-size:11px; color:#94a3b8;">
                                        <?= !empty($w['timestamp']) ? date('d M Y, h:i A', intval($w['timestamp'] / 1000)) : '' ?>
                                    </span>
                                </td>
                                <td>
                                    <strong><?= htmlspecialchars($w['userFullName'] ?? ($w['accountName'] ?? 'Player')) ?></strong><br>
                                    <span style="font-size:11px; color:#93c5fd;">ID: <?= htmlspecialchars($uid) ?></span><br>
                                    <span style="font-size:11px; color:#94a3b8;"><?= htmlspecialchars($w['userEmailOrPhone'] ?? '') ?></span>
                                </td>
                                <td>
                                    <strong style="color:#facc15; font-size:14px;"><?= htmlspecialchars($sym) ?><?= number_format($localAmt, 2) ?></strong><br>
                                    <span style="font-size:11px; color:#94a3b8;">Base: ৳<?= number_format($amtBdt, 2) ?></span>
                                </td>
                                <td>
                                    <span class="badge badge-pending"><?= htmlspecialchars($w['method'] ?? '') ?></span><br>
                                    <strong style="font-size:13px;"><?= htmlspecialchars($w['accountNumber'] ?? '') ?></strong><br>
                                    <span style="font-size:11px; color:#94a3b8;">Holder: <?= htmlspecialchars($w['accountName'] ?? '') ?></span>
                                </td>
                                <td>
                                    Level: <strong><?= intval($w['levelReached'] ?? 1) ?></strong><br>
                                    Ads: <strong><?= intval($w['adsWatched'] ?? 0) ?></strong>
                                </td>
                                <td><span class="badge <?= $badgeClass ?>"><?= htmlspecialchars($st) ?></span></td>
                                <td>
                                    <div style="display:flex; gap:6px; flex-wrap:wrap;">
                                        <?php if ($st !== 'PAID'): ?>
                                            <form method="POST" action="index.php?tab=withdrawals" style="display:inline;">
                                                <input type="hidden" name="form_type" value="withdrawal_action">
                                                <input type="hidden" name="ticket_code" value="<?= htmlspecialchars($ticket) ?>">
                                                <input type="hidden" name="new_status" value="PAID">
                                                <button type="submit" class="btn btn-success btn-sm">✔ Approve &amp; Paid</button>
                                            </form>
                                        <?php endif; ?>
                                        <?php if ($st !== 'REJECTED'): ?>
                                            <form method="POST" action="index.php?tab=withdrawals" style="display:inline;">
                                                <input type="hidden" name="form_type" value="withdrawal_action">
                                                <input type="hidden" name="ticket_code" value="<?= htmlspecialchars($ticket) ?>">
                                                <input type="hidden" name="user_id" value="<?= htmlspecialchars($uid) ?>">
                                                <input type="hidden" name="amount_bdt" value="<?= $amtBdt ?>">
                                                <input type="hidden" name="new_status" value="REJECTED">
                                                <button type="submit" class="btn btn-danger btn-sm">✕ Reject &amp; Refund</button>
                                            </form>
                                        <?php endif; ?>
                                        <form method="POST" action="index.php?tab=withdrawals" style="display:inline;" onsubmit="return confirm('Are you sure you want to delete this withdrawal ticket?');">
                                            <input type="hidden" name="form_type" value="withdrawal_action">
                                            <input type="hidden" name="ticket_code" value="<?= htmlspecialchars($ticket) ?>">
                                            <input type="hidden" name="new_status" value="DELETE">
                                            <button type="submit" class="btn btn-sm" style="background:#334155; color:#fff;">🗑</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if ($shown === 0): ?>
                            <tr><td colspan="7" style="text-align:center; padding:28px; color:#94a3b8;">No withdrawal requests found.</td></tr>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'ads'): ?>
            <!-- TAB 2: ADS CONTROL (ADMOB, UNITY ADS, FACEBOOK ADS + ON/OFF) -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('ads') ?> <span>Ads Control — AdMob, Unity &amp; Facebook Audience Network + ON/OFF</span></div>
                </div>
                <form method="POST" action="index.php?tab=ads">
                    <input type="hidden" name="form_type" value="save_ads_settings">

                    <div class="grid-3" style="margin-bottom:16px;">
                        <label class="switch-box">
                            <div>
                                <div style="font-weight:800; color:#facc15;">Master Ads Switch (ON / OFF)</div>
                                <div style="font-size:11px; color:#94a3b8;">Turn all ads in the Android app ON or OFF</div>
                            </div>
                            <input type="checkbox" name="ads_enabled" value="1" <?= !empty($config['ads_enabled']) ? 'checked' : '' ?>>
                        </label>

                        <label class="switch-box">
                            <div>
                                <div style="font-weight:800; color:#86efac;">Banner Ads (ON / OFF)</div>
                                <div style="font-size:11px; color:#94a3b8;">Bottom Banner Ad bar visibility</div>
                            </div>
                            <input type="checkbox" name="banner_ads_enabled" value="1" <?= !empty($config['banner_ads_enabled']) ? 'checked' : '' ?>>
                        </label>

                        <label class="switch-box">
                            <div>
                                <div style="font-weight:800; color:#93c5fd;">Rewarded Video Ads (ON / OFF)</div>
                                <div style="font-size:11px; color:#94a3b8;">Video ads for rewards, hints &amp; revives</div>
                            </div>
                            <input type="checkbox" name="rewarded_ads_enabled" value="1" <?= !empty($config['rewarded_ads_enabled']) ? 'checked' : '' ?>>
                        </label>
                    </div>

                    <div class="form-group" style="max-width:460px; margin-bottom:22px;">
                        <label class="form-label" style="color:#facc15; font-size:13px;">🎯 Select Active Ad Network to Display in App</label>
                        <select name="active_ad_network" class="form-select">
                            <option value="ADMOB" <?= ($config['active_ad_network'] ?? '') === 'ADMOB' ? 'selected' : '' ?>>Google AdMob (Active)</option>
                            <option value="UNITY" <?= ($config['active_ad_network'] ?? '') === 'UNITY' ? 'selected' : '' ?>>Unity Ads (Active)</option>
                            <option value="FACEBOOK" <?= ($config['active_ad_network'] ?? '') === 'FACEBOOK' ? 'selected' : '' ?>>Facebook (Meta) Audience Network (Active)</option>
                        </select>
                    </div>

                    <div class="grid-3">
                        <!-- 1. Google AdMob Codes -->
                        <div style="background:#0f172a; padding:16px; border-radius:14px; border:1px solid #3b82f6;">
                            <div style="font-weight:900; color:#60a5fa; margin-bottom:12px; font-size:15px;">1. Google AdMob Ad Unit Codes</div>
                            <div class="form-group">
                                <label class="form-label">AdMob App ID</label>
                                <input type="text" name="admob_app_id" class="form-input" value="<?= htmlspecialchars($config['admob_app_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">AdMob Banner Ad Unit ID</label>
                                <input type="text" name="admob_banner_id" class="form-input" value="<?= htmlspecialchars($config['admob_banner_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">AdMob Rewarded Video Ad Unit ID</label>
                                <input type="text" name="admob_rewarded_id" class="form-input" value="<?= htmlspecialchars($config['admob_rewarded_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">AdMob Interstitial Ad Unit ID</label>
                                <input type="text" name="admob_interstitial_id" class="form-input" value="<?= htmlspecialchars($config['admob_interstitial_id']) ?>">
                            </div>
                        </div>

                        <!-- 2. Unity Ads Codes -->
                        <div style="background:#0f172a; padding:16px; border-radius:14px; border:1px solid #a855f7;">
                            <div style="font-weight:900; color:#c084fc; margin-bottom:12px; font-size:15px;">2. Unity Ads Placement Codes</div>
                            <div class="form-group">
                                <label class="form-label">Unity Game ID (Android)</label>
                                <input type="text" name="unity_game_id" class="form-input" value="<?= htmlspecialchars($config['unity_game_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Unity Banner Placement ID</label>
                                <input type="text" name="unity_banner_id" class="form-input" value="<?= htmlspecialchars($config['unity_banner_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Unity Rewarded Video Placement ID</label>
                                <input type="text" name="unity_rewarded_id" class="form-input" value="<?= htmlspecialchars($config['unity_rewarded_id']) ?>">
                            </div>
                        </div>

                        <!-- 3. Facebook Audience Network Codes -->
                        <div style="background:#0f172a; padding:16px; border-radius:14px; border:1px solid #22c55e;">
                            <div style="font-weight:900; color:#4ade80; margin-bottom:12px; font-size:15px;">3. Facebook (Meta) Ads Codes</div>
                            <div class="form-group">
                                <label class="form-label">Facebook App ID</label>
                                <input type="text" name="fb_app_id" class="form-input" value="<?= htmlspecialchars($config['fb_app_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Facebook Banner Placement ID</label>
                                <input type="text" name="fb_banner_id" class="form-input" value="<?= htmlspecialchars($config['fb_banner_id']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Facebook Rewarded Video Placement ID</label>
                                <input type="text" name="fb_rewarded_id" class="form-input" value="<?= htmlspecialchars($config['fb_rewarded_id']) ?>">
                            </div>
                        </div>
                    </div>

                    <div style="margin-top:18px;">
                        <button type="submit" class="btn btn-success">💾 Save Ads Settings &amp; Sync to App</button>
                    </div>
                </form>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'min_withdraw'): ?>
            <!-- TAB 3: MINIMUM WITHDRAWAL & TIERS CONTROL -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('min_withdraw') ?> <span>Minimum Withdrawal, 4 Withdrawal Tiers &amp; Game Reward Control</span></div>
                </div>
                <form method="POST" action="index.php?tab=min_withdraw">
                    <input type="hidden" name="form_type" value="save_withdraw_settings">
                    <div class="grid-3">
                        <div class="form-group">
                            <label class="form-label" style="color:#facc15;">Minimum Withdrawal Amount (BDT ৳)</label>
                            <input type="number" step="0.01" name="min_withdraw_bdt" class="form-input" value="<?= htmlspecialchars($config['min_withdraw_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Withdrawal Tier 1 (BDT ৳)</label>
                            <input type="number" step="0.01" name="withdraw_tier1_bdt" class="form-input" value="<?= htmlspecialchars($config['withdraw_tier1_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Withdrawal Tier 2 (BDT ৳)</label>
                            <input type="number" step="0.01" name="withdraw_tier2_bdt" class="form-input" value="<?= htmlspecialchars($config['withdraw_tier2_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Withdrawal Tier 3 (BDT ৳)</label>
                            <input type="number" step="0.01" name="withdraw_tier3_bdt" class="form-input" value="<?= htmlspecialchars($config['withdraw_tier3_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Withdrawal Tier 4 (BDT ৳)</label>
                            <input type="number" step="0.01" name="withdraw_tier4_bdt" class="form-input" value="<?= htmlspecialchars($config['withdraw_tier4_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" style="color:#86efac;">Per Video Ad Watch Reward (BDT ৳)</label>
                            <input type="number" step="0.01" name="per_ad_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['per_ad_reward_bdt']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" style="color:#93c5fd;">Level Complete Video Ad Reward (BDT ৳)</label>
                            <input type="number" step="0.01" name="level_clear_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['level_clear_reward_bdt']) ?>" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-warning">💾 Save Minimum Withdrawal &amp; Tiers</button>
                </form>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'daily_tasks'): ?>
            <!-- TAB 4: DAILY TASKS CONTROL -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('daily_tasks') ?> <span>Daily Tasks Control (ON/OFF &amp; Reward Amounts in Base BDT ৳)</span></div>
                </div>
                <form method="POST" action="index.php?tab=daily_tasks">
                    <input type="hidden" name="form_type" value="save_tasks_settings">

                    <label class="switch-box" style="max-width:440px;">
                        <div>
                            <div style="font-weight:800; color:#facc15;">Daily Tasks Master Switch (ON / OFF)</div>
                            <div style="font-size:11px; color:#94a3b8;">Enable or disable Daily Tasks in the app's Task Center</div>
                        </div>
                        <input type="checkbox" name="daily_tasks_enabled" value="1" <?= !empty($config['daily_tasks_enabled']) ? 'checked' : '' ?>>
                    </label>

                    <div class="grid-3" style="margin-top:14px;">
                        <div class="form-group">
                            <label class="form-label">Daily Login Bonus (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_login_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_login_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Watch 1 Video Ad Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_ad1_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_ad1_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Complete 1 Level Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_lvl1_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_lvl1_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Complete 3 Levels Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_lvl3_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_lvl3_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Complete 5 Levels Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_lvl5_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_lvl5_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Complete 20 Levels Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_lvl20_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_lvl20_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Watch 3 Video Ads Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_ad3_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_ad3_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Watch 15 Video Ads Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_ad15_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_ad15_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Watch 30 Video Ads Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_ad30_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_ad30_reward_bdt']) ?>">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Watch 50 Video Ads Task (BDT ৳)</label>
                            <input type="number" step="0.01" name="task_ad50_reward_bdt" class="form-input" value="<?= htmlspecialchars($config['task_ad50_reward_bdt']) ?>">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-success">💾 Save Daily Tasks &amp; Sync to App</button>
                </form>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'server'): ?>
            <!-- TAB 5: APP-TO-WEB INTEGRATED SERVER & REPLACEMENT GUIDE -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('server') ?> <span>App-to-Web Integrated Server (Package Name &amp; Web URL)</span></div>
                    <a href="index.php?api=get_config&package_name=<?= urlencode($config['app_package_name']) ?>" target="_blank" class="btn btn-warning btn-sm">🔗 Test Live App JSON API</a>
                </div>
                <form method="POST" action="index.php?tab=server">
                    <input type="hidden" name="form_type" value="save_server_integration">
                    <div class="grid-2">
                        <div class="form-group">
                            <label class="form-label" style="color:#facc15;">Android App Package Name (App-to-Web Verification)</label>
                            <input type="text" name="app_package_name" class="form-input" value="<?= htmlspecialchars($config['app_package_name']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" style="color:#86efac;">Web Admin Panel Server URL (App Web URL)</label>
                            <input type="url" name="web_server_url" class="form-input" value="<?= htmlspecialchars($config['web_server_url']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label" style="color:#93c5fd;">Firebase Realtime Database URL</label>
                            <input type="url" name="firebase_db_url" class="form-input" value="<?= htmlspecialchars($config['firebase_db_url']) ?>" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Firebase Database Secret / Auth Token (Optional)</label>
                            <input type="password" name="firebase_secret" class="form-input" value="<?= htmlspecialchars($config['firebase_secret']) ?>" placeholder="Leave empty if database rules are public">
                        </div>
                    </div>

                    <label class="switch-box" style="max-width:540px;">
                        <div>
                            <div style="font-weight:800; color:#facc15;">Verify App Package Name on API Requests</div>
                            <div style="font-size:11px; color:#94a3b8;">Only allow requests matching <code><?= htmlspecialchars($config['app_package_name']) ?></code></div>
                        </div>
                        <input type="checkbox" name="enforce_package_check" value="1" <?= !empty($config['enforce_package_check']) ? 'checked' : '' ?>>
                    </label>

                    <button type="submit" class="btn btn-success">💾 Save App-to-Web Server Configuration</button>
                </form>
            </div>

            <div class="card">
                <div class="card-header">
                    <div class="card-title">📌 Code Replacement Reference (Android App &amp; Web Admin Panel)</div>
                </div>

                <h4 style="color:#facc15; margin-bottom:6px;">1. Android App Code — Web Server URL &amp; Firebase URL Location (`app/src/main/java/com/example/data/GameRepository.kt`):</h4>
                <div class="code-box">// File: app/src/main/java/com/example/data/GameRepository.kt (Lines 25-33)
const val WEB_ADMIN_SERVER_URL = "<?= htmlspecialchars($config['web_server_url']) ?>"
const val DEFAULT_FIREBASE_DB_URL = "<?= htmlspecialchars($config['firebase_db_url']) ?>"
const val APP_PACKAGE_NAME = "<?= htmlspecialchars($config['app_package_name']) ?>"</div>

                <h4 style="color:#86efac; margin-top:18px; margin-bottom:6px;">2. Web Admin Panel — Server &amp; Firebase Configuration Location (`admin-panel/index.php`):</h4>
                <div class="code-box">// File: admin-panel/index.php (Lines 19-39)
$defaultFirebaseUrl = '<?= htmlspecialchars($config['firebase_db_url']) ?>';
$defaultWebServerUrl = '<?= htmlspecialchars($config['web_server_url']) ?>';
$defaultAppPackage = '<?= htmlspecialchars($config['app_package_name']) ?>';</div>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'users'): ?>
            <!-- TAB 6: REGISTERED USERS & USER PROFILE EDITOR -->
            <div class="card">
                <div class="card-header">
                    <div class="card-title"><?= getMenuSvgIcon('users') ?> <span>Registered App User Profiles (<?= count($users) ?>)</span></div>
                </div>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>User ID</th>
                                <th>Full Name</th>
                                <th>Mobile / Email</th>
                                <th>Country</th>
                                <th>Balance (BDT)</th>
                                <th>Level Reached</th>
                                <th>Ads Watched</th>
                                <th>Saved Wallet</th>
                                <th>User Profile &amp; Password</th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php foreach ($users as $u):
                            $uid = $u['userId'] ?? '';
                            $uName = $u['fullName'] ?? '';
                            $uContact = $u['emailOrPhone'] ?? '';
                            $uBal = floatval($u['balanceBdt'] ?? 0);
                            $uLvl = intval($u['levelReached'] ?? 1);
                        ?>
                            <tr>
                                <td><strong style="color:#93c5fd;"><?= htmlspecialchars($uid) ?></strong></td>
                                <td><strong><?= htmlspecialchars($uName) ?></strong></td>
                                <td><?= htmlspecialchars($uContact) ?></td>
                                <td><?= htmlspecialchars($u['countryCode'] ?? 'BD') ?></td>
                                <td style="color:#facc15; font-weight:800;">৳<?= number_format($uBal, 2) ?></td>
                                <td>Level <?= $uLvl ?></td>
                                <td><?= intval($u['adsWatched'] ?? 0) ?></td>
                                <td><?= htmlspecialchars(($u['savedMethod'] ?? '') . ' ' . ($u['savedAccount'] ?? '')) ?></td>
                                <td>
                                    <button type="button" class="btn btn-primary btn-sm"
                                        onclick="openAppUserEditModal(<?= htmlspecialchars(json_encode($uid)) ?>, <?= htmlspecialchars(json_encode($uName)) ?>, <?= htmlspecialchars(json_encode($uContact)) ?>, <?= $uBal ?>, <?= $uLvl ?>)">
                                        👤 Edit Profile / Reset Pass
                                    </button>
                                </td>
                            </tr>
                        <?php endforeach; ?>
                        <?php if (empty($users)): ?>
                            <tr><td colspan="9" style="text-align:center; padding:28px; color:#94a3b8;">No registered users found yet.</td></tr>
                        <?php endif; ?>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Modal to Edit App User Profile & Reset App User Password -->
            <div class="save-popup-backdrop" id="editAppUserModal" style="display:none;">
                <div class="save-popup-card" style="max-width:480px; text-align:left; border-color:#3b82f6;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
                        <div style="font-size:18px; font-weight:900; color:#facc15;">👤 Edit App User Profile &amp; Password</div>
                        <button type="button" class="btn btn-danger btn-sm" onclick="document.getElementById('editAppUserModal').style.display='none'">✕</button>
                    </div>
                    <form method="POST" action="index.php?tab=users">
                        <input type="hidden" name="form_type" value="admin_edit_app_user">
                        <div class="form-group">
                            <label class="form-label">User ID</label>
                            <input type="text" name="target_user_id" id="modalTargetUserId" class="form-input" readonly>
                        </div>
                        <div class="form-group">
                            <label class="form-label">User Full Name</label>
                            <input type="text" name="edit_full_name" id="modalEditFullName" class="form-input" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Mobile Number or Email</label>
                            <input type="text" name="edit_email_phone" id="modalEditEmailPhone" class="form-input" required>
                        </div>
                        <div class="grid-2">
                            <div class="form-group">
                                <label class="form-label">Balance (BDT ৳)</label>
                                <input type="number" step="0.01" name="edit_balance_bdt" id="modalEditBalance" class="form-input" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Level Reached</label>
                                <input type="number" name="edit_level_reached" id="modalEditLevel" class="form-input" required>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label" style="color:#86efac;">Reset User Password (Forgot Password Reset)</label>
                            <input type="text" name="edit_new_password" class="form-input" placeholder="Leave blank to keep current password">
                        </div>
                        <button type="submit" class="btn btn-success btn-block">💾 Save User Profile &amp; Sync</button>
                    </form>
                </div>
            </div>
        <?php endif; ?>

        <?php if ($activeTab === 'profile'): ?>
            <!-- TAB 7: USER PROFILE, ACCOUNT DETAILS & FORGOT PASSWORD RECOVERY -->
            <div class="grid-2">
                <!-- Left Column: User Profile Summary Card -->
                <div class="card">
                    <div class="card-header">
                        <div class="card-title"><?= getMenuSvgIcon('profile') ?> <span>User Profile Overview</span></div>
                        <span class="badge badge-paid">Verified Active</span>
                    </div>
                    <div style="display:flex; align-items:center; gap:18px; padding:16px; background:#0f172a; border-radius:16px; border:1px solid #334155; margin-bottom:18px;">
                        <div class="user-avatar-circle" style="width:68px; height:68px; font-size:28px;">
                            <?= strtoupper(substr(trim($config['admin_name'] ?? 'A'), 0, 1)) ?>
                        </div>
                        <div>
                            <div style="font-size:20px; font-weight:900; color:#facc15;"><?= htmlspecialchars($config['admin_name']) ?></div>
                            <div style="font-size:13.5px; color:#93c5fd; margin-top:2px;"><?= htmlspecialchars($config['admin_username']) ?></div>
                            <div style="font-size:12px; color:#94a3b8; margin-top:4px;">
                                Role: <strong style="color:#fff;"><?= htmlspecialchars($config['admin_role']) ?></strong> • Phone: <strong style="color:#fff;"><?= htmlspecialchars($config['admin_phone']) ?></strong>
                            </div>
                        </div>
                    </div>

                    <div style="background:#0f172a; border:1px solid #334155; border-radius:14px; padding:16px;">
                        <div style="font-weight:900; color:#86efac; margin-bottom:10px; font-size:14px;">🔐 Account &amp; Recovery Information</div>
                        <div style="font-size:13px; color:#cbd5e1; line-height:1.8;">
                            <div>• <strong>Login Email / Username:</strong> <?= htmlspecialchars($config['admin_username']) ?></div>
                            <div>• <strong>Registered Mobile:</strong> <?= htmlspecialchars($config['admin_phone']) ?></div>
                            <div>• <strong>Forgot Password Recovery Key:</strong> <code><?= htmlspecialchars($config['recovery_code'] ?? 'cashpuzzle') ?></code></div>
                            <div>• <strong>Connected App Package:</strong> <code><?= htmlspecialchars($config['app_package_name']) ?></code></div>
                            <div>• <strong>Connected Web URL:</strong> <code><?= htmlspecialchars($config['web_server_url']) ?></code></div>
                        </div>
                    </div>
                </div>

                <!-- Right Column: Edit User Profile & Change Password / Recovery Key -->
                <div class="card" id="password-section">
                    <div class="card-header">
                        <div class="card-title"><?= getMenuSvgIcon('profile') ?> <span>Edit User Profile, Password &amp; Recovery Key</span></div>
                    </div>
                    <form method="POST" action="index.php?tab=profile">
                        <input type="hidden" name="form_type" value="update_profile">
                        <div class="grid-2">
                            <div class="form-group">
                                <label class="form-label">User Full Name</label>
                                <input type="text" name="admin_name" class="form-input" value="<?= htmlspecialchars($config['admin_name']) ?>" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Login Email / Username</label>
                                <input type="text" name="admin_username" class="form-input" value="<?= htmlspecialchars($config['admin_username']) ?>" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Mobile Phone Number</label>
                                <input type="text" name="admin_phone" class="form-input" value="<?= htmlspecialchars($config['admin_phone']) ?>">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Profile Role Title</label>
                                <input type="text" name="admin_role" class="form-input" value="<?= htmlspecialchars($config['admin_role']) ?>">
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="form-label" style="color:#facc15;">🔑 Forgot Password Recovery Key (Used to reset password if forgotten)</label>
                            <input type="text" name="recovery_code" class="form-input" value="<?= htmlspecialchars($config['recovery_code'] ?? 'cashpuzzle') ?>" required>
                        </div>

                        <div style="border-top:1px solid #334155; margin:16px 0; padding-top:14px;">
                            <div style="font-weight:900; color:#93c5fd; margin-bottom:10px; font-size:14px;">Change Account Password (Optional)</div>
                            <div class="grid-2">
                                <div class="form-group">
                                    <label class="form-label">New Password</label>
                                    <input type="password" name="new_password" class="form-input" placeholder="Leave blank to keep current">
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Confirm New Password</label>
                                    <input type="password" name="confirm_password" class="form-input" placeholder="Re-enter new password">
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-success btn-block">💾 Save User Profile &amp; Password</button>
                    </form>
                </div>
            </div>
        <?php endif; ?>

    <?php endif; ?>
</div>

<script>
function toggleQuickMenu() {
    var drawer = document.getElementById('quickMenuDrawer');
    var sidebar = document.getElementById('sidebarDrawer');
    var backdrop = document.getElementById('sidebarBackdrop');
    var btn = document.getElementById('menuToggleBtn');
    if (sidebar && backdrop) {
        sidebar.classList.toggle('open');
        backdrop.classList.toggle('open');
    }
    if (drawer) {
        drawer.classList.toggle('open');
    }
    if (btn) {
        btn.classList.toggle('active');
    }
}

function toggleProfileDropdown(e) {
    if (e) e.stopPropagation();
    var menu = document.getElementById('profileDropdownMenu');
    if (menu) {
        menu.classList.toggle('open');
    }
}

document.addEventListener('click', function() {
    var menu = document.getElementById('profileDropdownMenu');
    if (menu && menu.classList.contains('open')) {
        menu.classList.remove('open');
    }
});

function showForgotPassword(e) {
    if (e) e.preventDefault();
    var loginCard = document.getElementById('adminLoginCard');
    var forgotCard = document.getElementById('forgotPasswordCard');
    if (loginCard && forgotCard) {
        loginCard.style.display = 'none';
        forgotCard.style.display = 'block';
    }
}

function hideForgotPassword(e) {
    if (e) e.preventDefault();
    var loginCard = document.getElementById('adminLoginCard');
    var forgotCard = document.getElementById('forgotPasswordCard');
    if (loginCard && forgotCard) {
        forgotCard.style.display = 'none';
        loginCard.style.display = 'block';
    }
}

function openAppUserEditModal(uid, fullName, emailPhone, balance, level) {
    document.getElementById('modalTargetUserId').value = uid;
    document.getElementById('modalEditFullName').value = fullName;
    document.getElementById('modalEditEmailPhone').value = emailPhone;
    document.getElementById('modalEditBalance').value = balance;
    document.getElementById('modalEditLevel').value = level;
    document.getElementById('editAppUserModal').style.display = 'flex';
}
</script>

<?php if ($isLoggedIn): ?>
<script>
// Official Firebase Web Config for cashpuzzle (Auto-syncs settings from browser as well for free hosting compatibility)
const firebaseConfig = <?= json_encode($firebaseWebConfig, JSON_UNESCAPED_SLASHES) ?>;
const appSettingsPayload = <?= json_encode(buildAppSettingsPayload($config, true), JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE) ?>;

if (firebaseConfig && firebaseConfig.databaseURL) {
    fetch(firebaseConfig.databaseURL.replace(/\/$/, '') + '/app_settings.json', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(appSettingsPayload)
    }).catch(function() {});
}
</script>
<?php endif; ?>

</body>
</html>
