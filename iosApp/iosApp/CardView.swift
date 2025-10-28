import SwiftUI
import shared

/**
 * SwiftUI view for displaying a playing card
 */
struct CardView: View {
    let card: Card?
    let isSelected: Boolean
    let isPlayed: Bool
    let isRevealed: Bool
    let size: CardSize
    let onTap: (() -> Void)?

    enum CardSize {
        case small
        case medium
        case large
        case extraLarge

        var dimensions: CGSize {
            switch self {
            case .small: return CGSize(width: 60, height: 90)
            case .medium: return CGSize(width: 80, height: 120)
            case .large: return CGSize(width: 100, height: 150)
            case .extraLarge: return CGSize(width: 120, height: 180)
            }
        }
    }

    init(
        card: Card?,
        isSelected: Bool = false,
        isPlayed: Bool = false,
        isRevealed: Bool = true,
        size: CardSize = .medium,
        onTap: (() -> Void)? = nil
    ) {
        self.card = card
        self.isSelected = isSelected
        self.isPlayed = isPlayed
        self.isRevealed = isRevealed
        self.size = size
        self.onTap = onTap
    }

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 8)
                .fill(isRevealed && card != nil ? Color.white : Color.blue)
                .shadow(color: .black.opacity(0.2), radius: 3, x: 0, y: 2)

            if let card = card, isRevealed {
                VStack(spacing: 4) {
                    Text(rankString(card.rank))
                        .font(.system(size: size.dimensions.width * 0.3, weight: .bold))
                        .foregroundColor(suitColor(card.suit))

                    Text(suitSymbol(card.suit))
                        .font(.system(size: size.dimensions.width * 0.4))
                        .foregroundColor(suitColor(card.suit))
                }
            } else if !isRevealed {
                // Card back pattern
                RoundedRectangle(cornerRadius: 6)
                    .strokeBorder(Color.white, lineWidth: 2)
                    .background(RoundedRectangle(cornerRadius: 6).fill(Color.blue))
                    .padding(4)
            }
        }
        .frame(width: size.dimensions.width, height: size.dimensions.height)
        .opacity(isPlayed ? 0.4 : 1.0)
        .scaleEffect(isSelected ? 1.1 : 1.0)
        .animation(.spring(response: 0.3, dampingFraction: 0.6), value: isSelected)
        .onTapGesture {
            onTap?()
        }
    }

    private func rankString(_ rank: Rank) -> String {
        switch rank {
        case .ace: return "A"
        case .two: return "2"
        case .three: return "3"
        case .four: return "4"
        case .five: return "5"
        case .six: return "6"
        case .seven: return "7"
        case .eight: return "8"
        case .nine: return "9"
        case .ten: return "10"
        case .jack: return "J"
        case .queen: return "Q"
        case .king: return "K"
        default: return "?"
        }
    }

    private func suitSymbol(_ suit: Suit) -> String {
        switch suit {
        case .hearts: return "♥"
        case .diamonds: return "♦"
        case .clubs: return "♣"
        case .spades: return "♠"
        default: return "?"
        }
    }

    private func suitColor(_ suit: Suit) -> Color {
        switch suit {
        case .hearts, .diamonds: return .red
        case .clubs, .spades: return .black
        default: return .gray
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        CardView(
            card: Card(rank: .ace, suit: .hearts),
            size: .medium
        )

        CardView(
            card: Card(rank: .king, suit: .spades),
            isSelected: true,
            size: .medium
        )

        CardView(
            card: nil,
            isRevealed: false,
            size: .medium
        )
    }
    .padding()
}
