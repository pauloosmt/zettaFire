import "../global.css";
import { useEffect } from 'react';
import { Slot } from "expo-router";
import * as Notifications from 'expo-notifications';

Notifications.setNotificationHandler({
	handleNotification: async () => ({
		shouldShowAlert: true,
		shouldShowBanner: true,
		shouldShowList: true,
		shouldPlaySound: true,
		shouldSetBadge: true,
	}),
});

export default function RootLayout() {
	useEffect(() => {
		Notifications.setNotificationChannelAsync('alertas-fogo', {
			name: 'Alertas de Incêndio',
			importance: Notifications.AndroidImportance.MAX,
			sound: 'alertsound.wav',
			vibrationPattern: [0, 250, 250, 250],
			lightColor: '#EA580C',
		});
	}, []);

	return <Slot />;
}