import React, { useState } from 'react';
import { View, Text, Pressable, Image, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import AsyncStorage from '@react-native-async-storage/async-storage';
import api from '../../services/api';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

export default function LoginScreen() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleLogin = async () => {
        if (!email || !password) {
            Alert.alert("Atenção", "Preencha todos os campos.");
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/auth/login', { email, password });
            const { token } = response.data;

            await AsyncStorage.setItem('@zettafire:token', token);
            
            router.replace('/(tabs)/');
        } catch (error) {
            Alert.alert("Erro", "E-mail ou senha incorretos.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <ScreenWrapper className="justify-center">
            <View className="items-center mb-10 w-full">
                <View className="w-full h-48 mb-6 items-center justify-center">
                    <Image
                        source={require('../../assets/images/logo.png')}
                        className="w-full h-full"
                        resizeMode="contain"
                    />
                </View>
                <Text className="text-4xl font-bold text-text">Bem-te-vi</Text>
                <Text className="text-gray-500 text-center">Auxílio Contra Incêndios</Text>
            </View>

            <View className="mb-4">
                <Input
                    label="E-mail"
                    placeholder="seu@email.com"
                    keyboardType="email-address"
                    autoCapitalize="none"
                    value={email}
                    onChangeText={setEmail}
                />
                <Input
                    label="Senha"
                    placeholder="Sua senha"
                    secureTextEntry
                    value={password}
                    onChangeText={setPassword}
                />
            </View>

            <Pressable onPress={() => router.push('/(auth)/forgot-password')} className="items-end mb-8" disabled={loading}>
                <Text className="text-fire-orange font-semibold">Esqueceu a senha?</Text>
            </Pressable>

            <Button title="Entrar" onPress={handleLogin} isLoading={loading} />

            <Pressable onPress={() => router.push('/(auth)/register')} className="mt-6 items-center" disabled={loading}>
                <Text className="text-text font-semibold">
                    Novo por aqui? <Text className="text-fire-orange">Crie uma conta</Text>
                </Text>
            </Pressable>
        </ScreenWrapper>
    );
}