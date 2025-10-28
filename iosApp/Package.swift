// swift-tools-version:5.9
import PackageDescription

let package = Package(
    name: "Cribbage",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "Cribbage",
            targets: ["Cribbage"]
        )
    ],
    targets: [
        .target(
            name: "Cribbage",
            path: "iosApp"
        )
    ]
)
