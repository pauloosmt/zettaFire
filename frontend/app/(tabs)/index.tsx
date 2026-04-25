import { View, Text, FlatList, Pressable } from 'react-native';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { useRouter } from 'expo-router';

type StatusFire = 'ACTIVE' | 'CONTROLLED' | 'EXTINGUISHED';

interface FireEvent {
    idFireEvent: string;
    idFocoBdq: number;
    focoId: string;
    latitude: number;
    longitude: number;
    city: string;
    radiusOfRisk: number;
    startTime: string;
    fireRisk: number;
    frp: number;
    statusFire: StatusFire;
}

const MOCK_FIRES: FireEvent[] = [
    {
        idFireEvent: 'uuid-1',
        idFocoBdq: 101,
        focoId: 'foco-uuid-1',
        latitude: -21.2312,
        longitude: -44.9934,
        city: 'Lavras',
        radiusOfRisk: 1500,
        startTime: '2026-04-23T14:30:00',
        fireRisk: 95.5,
        frp: 120.4,
        statusFire: 'ACTIVE',
    },
    {
        idFireEvent: 'uuid-2',
        idFocoBdq: 102,
        focoId: 'foco-uuid-2',
        latitude: -19.9167,
        longitude: -43.9345,
        city: 'Belo Horizonte',
        radiusOfRisk: 500,
        startTime: '2026-04-22T08:15:00',
        fireRisk: 45.0,
        frp: 35.2,
        statusFire: 'CONTROLLED',
    },
    {
        idFireEvent: 'uuid-3',
        idFocoBdq: 103,
        focoId: 'foco-uuid-3',
        latitude: -15.7942,
        longitude: -47.8822,
        city: 'Brasília',
        radiusOfRisk: 0,
        startTime: '2026-04-20T18:45:00',
        fireRisk: 10.0,
        frp: 5.1,
        statusFire: 'EXTINGUISHED',
    }
];

const getStatusBadge = (status: StatusFire) => {
    switch (status) {
        case 'ACTIVE':
            return { label: 'Ativo', bg: 'bg-red-100', text: 'text-red-700' };
        case 'CONTROLLED':
            return { label: 'Controlado', bg: 'bg-yellow-100', text: 'text-yellow-700' };
        case 'EXTINGUISHED':
            return { label: 'Extinto', bg: 'bg-green-100', text: 'text-green-700' };
        default:
            return { label: 'Desconhecido', bg: 'bg-gray-100', text: 'text-gray-700' };
    }
};

const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', { 
        day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' 
    });
};

export default function FeedScreen() {
    const router = useRouter();

    const renderItem = ({ item }: { item: FireEvent }) => {
        const statusInfo = getStatusBadge(item.statusFire);

        return (
            <View className="bg-white p-5 rounded-2xl mb-4 border border-gray-200 shadow-sm">
                <View className="flex-row justify-between items-center mb-2">
                    <Text className="text-xl font-bold text-text">{item.city}</Text>
                    <View className={`px-3 py-1 rounded-full ${statusInfo.bg}`}>
                        <Text className={`text-xs font-bold ${statusInfo.text}`}>
                            {statusInfo.label}
                        </Text>
                    </View>
                </View>

                <Text className="text-sm text-gray-500 mb-4">
                    Identificado em: {formatDate(item.startTime)}
                </Text>

                <View className="flex-row justify-between bg-gray-50 p-3 rounded-xl mb-4">
                    <View className="items-center">
                        <Text className="text-xs text-gray-500">Risco</Text>
                        <Text className="font-bold text-text">{item.fireRisk.toFixed(1)}%</Text>
                    </View>
                    <View className="items-center">
                        <Text className="text-xs text-gray-500">FRP</Text>
                        <Text className="font-bold text-text">{item.frp.toFixed(1)}</Text>
                    </View>
                    <View className="items-center">
                        <Text className="text-xs text-gray-500">Raio de Risco</Text>
                        <Text className="font-bold text-text">{item.radiusOfRisk}m</Text>
                    </View>
                </View>

                <Pressable 
                    className="bg-fire-orange py-3 rounded-xl items-center"
                    onPress={() => {
                        router.push({
                            pathname: '/map',
                            params: {
                                lat: item.latitude,
                                lng: item.longitude
                            }
                        });
                    }}
                >
                    <Text className="text-white font-bold">Ver no Mapa</Text>
                </Pressable>
            </View>
        );
    };

    return (
        <ScreenWrapper>
            <View className="px-5 pt-6 pb-2">
                <Text className="text-3xl font-bold text-text">Feed de Alertas</Text>
                <Text className="text-gray-500 mb-6">Acompanhe os focos de incêndio recentes.</Text>
            </View>

            <FlatList
                data={MOCK_FIRES}
                keyExtractor={(item) => item.idFireEvent}
                renderItem={renderItem}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 40 }}
                showsVerticalScrollIndicator={false}
            />
        </ScreenWrapper>
    );
}