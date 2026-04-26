import React, { useEffect, useRef, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import MapView, { Heatmap, PROVIDER_GOOGLE } from 'react-native-maps';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Feather } from '@expo/vector-icons';
import { useLocalSearchParams } from 'expo-router';
import api from '../../services/api';

interface FireEvent {
    id: string;
    city: string;
    latitude: number;
    longitude: number;
    status_fire: string;
    fire_risk: number;
    start_time: string;
}

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

    const [fires, setFires] = useState<FireEvent[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchFires = async () => {
        setLoading(true);
        try {
            const response = await api.get('/fire-event/all', { params: { page: 0, size: 100 } });
            const data = response.data.content || response.data;
            setFires(Array.isArray(data) ? data : []);
        } catch (error) {
            Alert.alert("Erro", "Falha ao sincronizar dados de satélite.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFires();
    }, []);

    useEffect(() => {
        if (lat && lng) {
            mapRef.current?.animateToRegion({
                latitude: Number(lat),
                longitude: Number(lng),
                latitudeDelta: 0.02,
                longitudeDelta: 0.02,
            }, 1000);
        }
    }, [lat, lng]);

    const heatmapPoints = fires.map(fire => {
        const risk = (fire.fire_risk != null && fire.fire_risk > 0) ? fire.fire_risk : 0.2;
        return {
            latitude: fire.latitude,
            longitude: fire.longitude,
            weight: risk <= 1 ? risk : risk / 100,
        };
    });

    return (
        <View style={styles.container}>
            <View style={[styles.header, { paddingTop: insets.top + 12 }]}>
                <View>
                    <Text style={styles.headerTitle}>Mapa de Calor</Text>
                    <Text style={styles.headerSubtitle}>
                        {fires.length} focos detectados em MG
                    </Text>
                </View>
                <TouchableOpacity style={styles.refreshButton} onPress={fetchFires}>
                    <Feather name="refresh-cw" size={20} color="#EA580C" />
                </TouchableOpacity>
            </View>

            <MapView
                ref={mapRef}
                provider={PROVIDER_GOOGLE}
                style={styles.map}
                initialRegion={MG_REGION}
                mapType="hybrid"
                minZoomLevel={5}
                showsUserLocation={false}
            >
                {heatmapPoints.length > 0 && (
                    <Heatmap
                        points={heatmapPoints}
                        radius={60}
                        opacity={0.8}
                        gradient={{
                            colors: ['transparent', '#fcd34d', '#ea580c', '#b91c1c'],
                            startPoints: [0.01, 0.25, 0.6, 1],
                            colorMapSize: 256,
                        }}
                    />
                )}
            </MapView>

            <TouchableOpacity
                style={styles.resetFab}
                onPress={() => mapRef.current?.animateToRegion(MG_REGION)}
            >
                <Feather name="map" size={24} color="#FFF" />
            </TouchableOpacity>

            {loading && (
                <View style={styles.loadingOverlay}>
                    <ActivityIndicator size="large" color="#EA580C" />
                </View>
            )}
        </View>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, backgroundColor: '#000' },
    header: {
        position: 'absolute',
        top: 0, width: '100%',
        backgroundColor: 'rgba(255, 255, 255, 0.9)',
        paddingHorizontal: 20,
        paddingBottom: 12,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        zIndex: 10,
        borderBottomWidth: 1,
        borderBottomColor: '#E5E7EB',
    },
    headerTitle: { fontSize: 18, fontWeight: 'bold', color: '#1F2937' },
    headerSubtitle: { fontSize: 12, color: '#6B7280' },
    refreshButton: {
        width: 40, height: 40, borderRadius: 20,
        backgroundColor: '#FFF7ED', justifyContent: 'center', alignItems: 'center',
    },
    map: { flex: 1 },
    loadingOverlay: {
        ...StyleSheet.absoluteFillObject,
        backgroundColor: 'rgba(0,0,0,0.3)',
        justifyContent: 'center',
        alignItems: 'center',
        zIndex: 20,
    },
    resetFab: {
        position: 'absolute',
        bottom: 30,
        right: 20,
        width: 56,
        height: 56,
        borderRadius: 28,
        backgroundColor: '#EA580C',
        justifyContent: 'center',
        alignItems: 'center',
        elevation: 6,
    }
});