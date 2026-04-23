import { View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ReactNode } from 'react';

interface ScreenWrapperProps {
    children: ReactNode;
    className?: string;
}

export function ScreenWrapper({ children, className = '' }: ScreenWrapperProps) {
    return (
        <SafeAreaView className="flex-1 bg-background">
            <View className={`flex-1 px-6 ${className}`}>
                {children}
            </View>
        </SafeAreaView>
    );
}