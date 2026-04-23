import { View, Text, Pressable, Image, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { ScreenWrapper } from '../../components/layout/ScreenWrapper';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';

export default function RegisterScreen() {
    const router = useRouter();

    return (
        <ScreenWrapper>
            <ScrollView showsVerticalScrollIndicator={false}>
                <View className="items-center mt-6 mb-8 w-full">
                    <View className="w-full h-48 mb-4 items-center justify-center">
                        <Image
                            source={require('../../assets/images/logo.png')}
                            className="w-full h-full"
                            resizeMode="contain"
                        />
                    </View>
                    <Text className="text-3xl font-bold text-text">Criar Conta</Text>
                </View>

                <View className="gap-1">
                    <Input
                        label="Nome Completo"
                        placeholder="Como quer ser chamado?"
                    />
                    <Input
                        label="E-mail"
                        placeholder="seu@email.com"
                        keyboardType="email-address"
                        autoCapitalize="none"
                    />
                    <Input
                        label="Senha"
                        placeholder="Crie uma senha forte"
                        secureTextEntry
                    />
                    <Input
                        label="Confirmar Senha"
                        placeholder="Repita a senha"
                        secureTextEntry
                    />
                </View>

                <Button
                    title="Cadastrar"
                    variant="green"
                    className="mt-6"
                    onPress={() => router.replace('/(tabs)/')}
                />

                <Pressable onPress={() => router.back()} className="mt-6 mb-10 items-center">
                    <Text className="text-gray-500">
                        Já tem conta? <Text className="text-fire-orange font-semibold">Faça Login</Text>
                    </Text>
                </Pressable>
            </ScrollView>
        </ScreenWrapper>
    );
}