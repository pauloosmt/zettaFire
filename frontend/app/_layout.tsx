import "../global.css";
import { useEffect } from 'react';
import { Slot, useRouter } from "expo-router";
import * as Notifications from 'expo-notifications';

Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: true,
        shouldShowBanner: true,
        shouldShowList: true,
    }),
});

export default function RootLayout() {
    const router = useRouter();

    useEffect(() => {
        Notifications.setNotificationChannelAsync('alertas-fogo', {
            name: 'Alertas de Incêndio',
            importance: Notifications.AndroidImportance.MAX,
            sound: 'alertsound.wav',
            vibrationPattern: [0, 250, 250, 250],
            lightColor: '#EA580C',
        });

        const foregroundSubscription = Notifications.addNotificationReceivedListener(notification => {
            console.log("Notificação em primeiro plano:", notification);
        });

        const responseSubscription = Notifications.addNotificationResponseReceivedListener(() => {
            router.push('/(tabs)/map');
        });

        return () => {
            foregroundSubscription.remove();
            responseSubscription.remove();
        };
    }, []);

    return <Slot />;
}