import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, Pressable, ActivityIndicator, RefreshControl, Alert } from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { useRouter } from 'expo-router';
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

export default function FeedScreen() {
    const router = useRouter();
    const [fires, setFires] = useState<FireEvent[]>([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);

    const capitalizeCity = (city: string) => {
        if (!city) return "Local não identificado";
        return city.toLowerCase().split(' ').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ');
    };

    const fetchFires = async () => {
        try {
            const response = await api.get('/fire-event/all', { params: { page: 0, size: 20 } });
            const data = response.data.content || response.data;
            console.log("Fires fetched:", data);
            setFires(Array.isArray(data) ? data : []);
        } catch (error: any) {
            if (error.response?.status === 403) {
                router.replace('/(auth)/login');
            } else {
                Alert.alert("Erro", "Não foi possível carregar os focos.");
            }
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => { fetchFires(); }, []);

    const onRefresh = useCallback(() => {
        setRefreshing(true);
        fetchFires();
    }, []);

    const renderItem = ({ item }: { item: FireEvent }) => {
        const getStatusBadge = (status: string) => {
            switch (status) {
                case 'ACTIVE':
                    return { label: 'Ativo', bg: 'bg-red-100', text: 'text-red-700' };
                case 'CONTROLLED':
                    return { label: 'Controlado', bg: 'bg-yellow-100', text: 'text-yellow-700' };
                default:
                    return { label: 'Extinto', bg: 'bg-green-100', text: 'text-green-700' };
            }
        };

        const statusInfo = getStatusBadge(item.status_fire);

        const formatDate = (dateStr: string) => {
            if (!dateStr) return '-';
            try {
                const date = new Date(dateStr);
                return date.toLocaleDateString('pt-BR', {
                    day: '2-digit',
                    month: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit'
                });
            } catch { return '-'; }
        };

        const formatRisk = (risk: number | null | undefined) => {
            if (risk == null) return "N/D";
            const pct = risk <= 1 ? risk * 100 : risk;
            return pct.toFixed(1) + "%";
        };

        return (
            <View className="bg-white p-5 rounded-2xl mb-4 mx-5 border border-gray-200 shadow-sm">
                <View className="flex-row justify-between items-center mb-2">
                    <Text className="text-xl font-bold text-text flex-1 mr-2">
                        {capitalizeCity(item.city)}
                    </Text>
                    <View className={`px-3 py-1 rounded-full ${statusInfo.bg}`}>
                        <Text className={`text-xs font-bold ${statusInfo.text}`}>{statusInfo.label}</Text>
                    </View>
                </View>

                <View className="flex-row justify-between bg-gray-50 p-3 rounded-xl mb-4">
                    <View className="items-center">
                        <Text className="text-xs text-gray-500">Risco</Text>
                        <Text className="font-bold text-text">{formatRisk(item.fire_risk)}</Text>
                    </View>
                    <View className="items-center">
                        <Text className="text-xs text-gray-500">Horário</Text>
                        <Text className="font-bold text-text">{formatDate(item.start_time)}</Text>
                    </View>
                </View>

                <Pressable
                    className="bg-fire-orange py-3 rounded-xl items-center active:opacity-80"
                    onPress={() => router.push({
                        pathname: '/(tabs)/map',
                        params: {
                            lat: item.latitude,
                            lng: item.longitude,
                            city: capitalizeCity(item.city)
                        }
                    })}
                >
                    <Text className="text-white font-bold">Ver no Mapa</Text>
                </Pressable>
            </View>
        );
    };

    if (loading) {
        return (
            <ScreenWrapper>
                <View className="flex-1 justify-center items-center">
                    <ActivityIndicator size="large" color="#EA580C" />
                </View>
            </ScreenWrapper>
        );
    }

    return (
        <ScreenWrapper>
            <FlatList
                data={fires}
                keyExtractor={(item, index) => item.id?.toString() || index.toString()}
                renderItem={renderItem}
                ListHeaderComponent={() => (
                    <View className="px-5 pt-6 pb-2">
                        <Text className="text-3xl font-bold text-text">Feed de Alertas</Text>
                        <Text className="text-gray-500 mb-6">Acompanhe os focos de incêndio em tempo real.</Text>
                    </View>
                )}
                contentContainerStyle={{ paddingBottom: 40 }}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={["#EA580C"]} />
                }
                ListEmptyComponent={
                    <View className="mt-20 items-center">
                        <Text className="text-gray-400">Nenhum foco encontrado.</Text>
                    </View>
                }
            />
        </ScreenWrapper>
    );
}