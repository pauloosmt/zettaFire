import React, { useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import MapView, { Heatmap, PROVIDER_GOOGLE } from 'react-native-maps';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import { useLocalSearchParams } from 'expo-router';

interface FireEvent {
    idFireEvent: string;
    latitude: number;
    longitude: number;
    fireRisk: number;
}

const MOCK_FIRES: FireEvent[] = [
    { idFireEvent: '1', latitude: -19.9167, longitude: -43.9345, fireRisk: 100 },
    { idFireEvent: '2', latitude: -19.9500, longitude: -43.9000, fireRisk: 80 },
    { idFireEvent: '3', latitude: -19.7672, longitude: -43.8528, fireRisk: 90 },
    { idFireEvent: '4', latitude: -18.9186, longitude: -48.2772, fireRisk: 60 },
    { idFireEvent: '5', latitude: -21.7664, longitude: -43.3496, fireRisk: 85 },
    { idFireEvent: '6', latitude: -16.7333, longitude: -43.8667, fireRisk: 95 },
    { idFireEvent: '7', latitude: -21.2312, longitude: -44.9934, fireRisk: 95.5},
];

const MG_REGION = {
    latitude: -18.5122,
    longitude: -44.5550,
    latitudeDelta: 8.0,
    longitudeDelta: 8.0,
};

export default function MapScreen() {
    const insets = useSafeAreaInsets();
    const mapRef = useRef<MapView>(null);
    const { lat, lng } = useLocalSearchParams();

    const heatmapPoints = MOCK_FIRES.map(fire => ({
        latitude: fire.latitude,
        longitude: fire.longitude,
        weight: fire.fireRisk,
    }));

    useEffect(() => {
        if (lat && lng) {
            mapRef.current?.animateToRegion({
                latitude: Number(lat),
                longitude: Number(lng),
                latitudeDelta: 0.1,
                longitudeDelta: 0.1,
            }, 1500);
        }
    }, [lat, lng]);

    return (
        <View style={styles.container}>
            <View style={[styles.header, { paddingTop: insets.top + 16 }]}>
                <View>
                    <Text style={styles.headerTitle}>Mapa de Risco</Text>
                    <Text style={styles.headerSubtitle}>Monitoramento em Tempo Real - MG</Text>
                </View>
                <TouchableOpacity style={styles.filterButton}>
                    <Feather name="filter" size={20} color="#EA580C" />
                </TouchableOpacity>
            </View>

            <MapView
                ref={mapRef}
                provider={PROVIDER_GOOGLE}
                style={styles.map}
                initialRegion={MG_REGION}
                minZoomLevel={5}
                maxZoomLevel={15}
                showsUserLocation={true}
                showsMyLocationButton={false}
                mapType="terrain"
            >
                <Heatmap
                    points={heatmapPoints}
                    radius={20}
                    opacity={0.8}
                    gradient={{
                        colors: ['transparent', '#fcd34d', '#ea580c', '#b91c1c'],
                        startPoints: [0.01, 0.25, 0.6, 1],
                        colorMapSize: 256,
                    }}
                />
            </MapView>

            <TouchableOpacity style={styles.fab}>
                <Feather name="refresh-cw" size={24} color="#FFF" />
            </TouchableOpacity>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#F3F4F6',
    },
    header: {
        backgroundColor: '#FFFFFF',
        paddingHorizontal: 20,
        paddingBottom: 16,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        borderBottomWidth: 1,
        borderBottomColor: '#E5E7EB',
        zIndex: 10,
        elevation: 5,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: 'bold',
        color: '#1F2937',
    },
    headerSubtitle: {
        fontSize: 12,
        color: '#6B7280',
        marginTop: 2,
    },
    filterButton: {
        width: 40,
        height: 40,
        borderRadius: 20,
        backgroundColor: '#FFF7ED',
        justifyContent: 'center',
        alignItems: 'center',
    },
    map: {
        flex: 1,
        width: '100%',
    },
    fab: {
        position: 'absolute',
        bottom: 24,
        right: 24,
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: '#EA580C',
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 6,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 3 },
        shadowOpacity: 0.3,
        shadowRadius: 4,
    }
});