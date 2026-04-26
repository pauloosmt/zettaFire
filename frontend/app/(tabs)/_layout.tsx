import React, { useEffect, useState } from 'react';
import { Tabs } from 'expo-router';
import { Feather } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { jwtDecode } from "jwt-decode";

export default function TabLayout() {
    const insets = useSafeAreaInsets();
    const [userRole, setUserRole] = useState<string | null>(null);

    useEffect(() => {
        async function checkSession() {
            try {
                const token = await AsyncStorage.getItem('@zettafire:token');
                if (token) {
                    const decoded: any = jwtDecode(token);
    
                    const rawRole = decoded.role || "";
                    
                    if (rawRole.includes('ADMIN')) {
                        setUserRole('ADMIN');
                        await AsyncStorage.setItem('@zettafire:userRole', 'ADMIN');
                    } else {
                        setUserRole('USER');
                        await AsyncStorage.setItem('@zettafire:userRole', 'USER');
                    }
                }
            } catch (error) {
                console.error("Erro na sessão:", error);
            }
        }
        checkSession();
    }, []);

    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarActiveTintColor: '#EA580C',
                tabBarStyle: {
                    height: 60 + insets.bottom,
                    paddingBottom: 8 + insets.bottom,
                    paddingTop: 8,
                }
            }}
        >
            <Tabs.Screen
                name="index"
                options={{
                    title: 'Feed',
                    tabBarIcon: ({ color }) => <Feather name="list" size={24} color={color} />,
                }}
            />
            <Tabs.Screen
                name="map"
                options={{
                    title: 'Mapa',
                    tabBarIcon: ({ color }) => <Feather name="map" size={24} color={color} />,
                }}
            />
            <Tabs.Screen
                name="dashboard"
                options={{
                    title: 'Painel',
                    href: userRole === 'ADMIN' ? '/(tabs)/dashboard' : null,
                    tabBarIcon: ({ color }) => <Feather name="pie-chart" size={24} color={color} />,
                }}
            />
        </Tabs>
    );
}