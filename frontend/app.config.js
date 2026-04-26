export default {
  expo: {
    name: "Bem-te-vi",
    slug: "zetta-fire",
    version: "1.0.0",
    orientation: "portrait",
    icon: "./assets/images/logo_square.png",
    scheme: "zettafire",
    userInterfaceStyle: "light",
    newArchEnabled: true,
    ios: {
      supportsTablet: true,
    },
    android: {
      adaptiveIcon: {
        backgroundColor: "#F6F4EB",
        foregroundImage: "./assets/images/logo_square.png",
      },
      edgeToEdgeEnabled: true,
      predictiveBackGestureEnabled: false,
      package: "com.henrique117.zettafire",
      config: {
        googleMaps: {
          apiKey: process.env.GOOGLE_MAPS_API_KEY,
        },
      },
    },
    web: {
      output: "static",
      favicon: "./assets/images/logo_square.png",
    },
    notification: {
      icon: "./assets/images/logo_square.png",
      color: "#EA580C",
    },
    plugins: [
      "expo-router",
      [
        "expo-splash-screen",
        {
          image: "./assets/images/logo_square.png",
          imageWidth: 200,
          resizeMode: "contain",
          backgroundColor: "#F6F4EB",
        },
      ],
      [
        "expo-notifications",
        {
          sounds: ["./assets/sounds/alertsound.wav"],
        },
      ],
    ],
    experiments: {
      typedRoutes: true,
      reactCompiler: true,
    },
    extra: {
      router: {},
      eas: {
        projectId: "1f1a0a34-eda1-4180-a12b-146a43fdbd14",
      },
    },
  },
};