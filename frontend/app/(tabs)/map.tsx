import React, { useEffect, useRef, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, Alert } from 'react-native';
import MapView, { Heatmap, PROVIDER_GOOGLE, Marker, Callout } from 'react-native-maps';
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

    const capitalizeCity = (name: string) => {
        if (!name) return "Local";
        return name.toLowerCase().split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
    };

    const formatRisk = (risk: number | null | undefined) => {
        if (risk == null) return "N/D";
        const pct = risk <= 1 ? risk * 100 : risk;
        return pct.toFixed(1) + "%";
    };

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

    const heatmapPoints = fires
        .filter(f => f.fire_risk != null && f.fire_risk > 0)
        .map(fire => ({
            latitude: fire.latitude,
            longitude: fire.longitude,
            weight: fire.fire_risk <= 1 ? fire.fire_risk : fire.fire_risk / 100,
        }));

    return (
        <View style={styles.container}>
            <View style={[styles.header, { paddingTop: insets.top + 12 }]}>
                <View>
                    <Text style={styles.headerTitle}>Mapa de Risco</Text>
                    <Text style={styles.headerSubtitle}>
                        {fires.length} focos detectados via Google Maps
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
                        radius={40}
                        opacity={0.7}
                        gradient={{
                            colors: ['transparent', '#fcd34d', '#ea580c', '#b91c1c'],
                            startPoints: [0.01, 0.25, 0.6, 1],
                            colorMapSize: 256,
                        }}
                    />
                )}

                {fires.map((fire) => (
                    <Marker
                        key={fire.id}
                        coordinate={{ latitude: fire.latitude, longitude: fire.longitude }}
                        pinColor={fire.fire_risk != null && fire.fire_risk > 0.7 ? "#b91c1c" : "#ea580c"}
                    >
                        <Callout tooltip>
                            <View style={styles.calloutContainer}>
                                <Text style={styles.calloutTitle}>{capitalizeCity(fire.city)}</Text>
                                <Text style={styles.calloutDesc}>Risco: {formatRisk(fire.fire_risk)}</Text>
                                <Text style={styles.calloutDesc}>Status: {fire.status_fire}</Text>
                                <View style={styles.calloutArrow} />
                            </View>
                        </Callout>
                    </Marker>
                ))}
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
    },
    calloutContainer: {
        width: 160, backgroundColor: '#FFF', borderRadius: 8,
        padding: 10, marginBottom: 5, borderColor: '#E5E7EB', borderWidth: 1,
    },
    calloutTitle: { fontWeight: 'bold', fontSize: 14, color: '#1F2937' },
    calloutDesc: { fontSize: 12, color: '#4B5563' },
    calloutArrow: {
        width: 0, height: 0, backgroundColor: 'transparent', borderStyle: 'solid',
        borderLeftWidth: 8, borderRightWidth: 8, borderTopWidth: 10,
        borderLeftColor: 'transparent', borderRightColor: 'transparent', borderTopColor: '#FFF',
        alignSelf: 'center', marginTop: -1,
    }
});