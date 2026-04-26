import { Pressable, Text, ActivityIndicator } from 'react-native';

interface ButtonProps {
    title: string;
    onPress: () => void;
    variant?: 'orange' | 'yellow' | 'green' | 'outline';
    isLoading?: boolean;
    disabled?: boolean;
    className?: string;
}

export function Button({
    title,
    onPress,
    variant = 'orange',
    isLoading = false,
    disabled = false,
    className = ''
}: ButtonProps) {

    const variantClasses = {
        orange: "bg-fire-orange",
        yellow: "bg-fire-yellow",
        green: "bg-fire-green",
        outline: "bg-transparent border-2 border-fire-orange",
    };

    const textClasses = {
        orange: "text-white",
        yellow: "text-text",
        green: "text-white",
        outline: "text-fire-orange",
    };

    const isDisabled = isLoading || disabled;

    return (
        <Pressable
            onPress={onPress}
            disabled={isDisabled}
            className={`py-4 rounded-xl items-center justify-center active:opacity-80 ${variantClasses[variant]} ${isDisabled ? 'opacity-50' : ''} ${className}`}
        >
            {isLoading ? (
                <ActivityIndicator color={variant === 'outline' ? "#EA580C" : "#FFFFFF"} />
            ) : (
                <Text className={`${textClasses[variant]} font-bold text-lg`}>
                    {title}
                </Text>
            )}
        </Pressable>
    );
}