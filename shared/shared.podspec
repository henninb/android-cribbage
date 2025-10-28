Pod::Spec.new do |spec|
    spec.name                     = 'shared'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/brianhenning/android-cribbage'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'Brian Henning'
    spec.license                  = 'MIT'
    spec.summary                  = 'Shared Kotlin Multiplatform logic for Cribbage game'

    spec.vendored_frameworks      = "build/bin/iosSimulatorArm64/debugFramework/shared.framework"
    spec.libraries                = "c++"
    spec.ios.deployment_target    = '14.0'

    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':shared',
        'PRODUCT_MODULE_NAME' => 'shared',
    }

    spec.script_phases = [
        {
            :name => 'Build shared Framework',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT/.."
                "$REPO_ROOT/gradlew" -p "$REPO_ROOT" :shared:linkDebugFrameworkIosSimulatorArm64
            SCRIPT
        }
    ]
end
