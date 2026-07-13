import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, MessageCircle, X, Send, Bot, User, Sparkles } from 'lucide-angular';
import { ChatbotService } from '../../core/services/chatbot.service';

interface Message {
  text: string;
  sender: 'user' | 'bot';
  timestamp: Date;
  options?: string[];
}

const STATIC_FLOWS: Record<string, { text: string, options: string[] }> = {
  "hi": { text: "Hello! How can I help you today?", options: ["Plans & Operators", "Payment Issue", "Recharge Issue", "Feedback"] },
  "hello": { text: "Hello! How can I help you today?", options: ["Plans & Operators", "Payment Issue", "Recharge Issue", "Feedback"] },
  "main menu": { text: "Main Menu. How can I help you?", options: ["Plans & Operators", "Payment Issue", "Recharge Issue", "Feedback"] },
  
  "plans & operators": { text: "Sure! Please select an operator to view their top plans:", options: ["Jio Plans", "Airtel Plans", "Vi Plans", "BSNL Plans", "Main Menu"] },
  "jio plans": { text: "Jio's top trending plans are: ₹299 (2GB/day, 28 days), ₹666 (1.5GB/day, 84 days), and ₹2999 (2.5GB/day, 365 days).", options: ["Other Operators", "Main Menu"] },
  "airtel plans": { text: "Airtel's top plans include: ₹299 (1.5GB/day, 28 days), ₹719 (1.5GB/day, 84 days), and ₹2999 (2GB/day, 365 days) with unlimited 5G data.", options: ["Other Operators", "Main Menu"] },
  "vi plans": { text: "Vi (Vodafone Idea) offers Hero Unlimited plans like: ₹299 (1.5GB/day, 28 days) and ₹719 (1.5GB/day, 84 days) with weekend data rollover.", options: ["Other Operators", "Main Menu"] },
  "bsnl plans": { text: "BSNL provides highly affordable plans: ₹153 (1GB/day, 28 days), ₹599 (3GB/day, 84 days), and great long-term validity options.", options: ["Other Operators", "Main Menu"] },
  "other operators": { text: "Please select an operator to view their top plans:", options: ["Jio Plans", "Airtel Plans", "Vi Plans", "BSNL Plans", "Main Menu"] },

  "payment issue": { text: "I understand you're facing a payment issue. Please select a specific category:", options: ["Amount Deducted", "Payment Failed", "Main Menu"] },
  "amount deducted": { text: "If your amount was deducted but the recharge failed, it usually refunds automatically to your original payment method within 3-5 business days.", options: ["Check History", "Main Menu"] },
  "payment failed": { text: "Payment failures can happen due to bank server timeouts. Please wait 10 minutes before retrying your payment.", options: ["Main Menu"] },
  "check history": { text: "You can view your transaction history by clicking 'History' in the left menu dashboard.", options: ["Main Menu"] },
  
  "recharge issue": { text: "What seems to be the problem with your recharge?", options: ["Plan Not Active", "Wrong Number", "Main Menu"] },
  "plan not active": { text: "Sometimes telco activation takes up to 15 minutes. Please try restarting your phone. If it still doesn't work, contact your network provider.", options: ["Main Menu"] },
  "wrong number": { text: "Unfortunately, recharges sent to the wrong number cannot be reversed. Please double-check the number next time before confirming payment.", options: ["Main Menu"] },

  "feedback": { text: "We value your feedback! Please type your feedback below and hit Send.", options: ["Main Menu"] },
  "general inquiry": { text: "For general inquiries, you can upload documents and ask me questions about them! Go ahead and type your question below.", options: ["Main Menu"] }
};

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  template: `
    <!-- Chat Toggle Button -->
    <button class="chat-toggle" (click)="toggleChat()" [class.hidden]="isOpen()">
      <lucide-icon name="message-circle" [size]="28"></lucide-icon>
      <span class="pulse"></span>
    </button>

    <!-- Chat Window -->
    <div class="chat-window glass" [class.open]="isOpen()">
      <div class="chat-header">
        <div class="header-info">
          <div class="bot-avatar">
            <lucide-icon name="sparkles" [size]="18" class="gold-icon"></lucide-icon>
          </div>
          <div>
            <h3>Hub Assistant</h3>
            <span class="status">AI Powered • Online</span>
          </div>
        </div>
        <button class="close-btn" (click)="toggleChat()">
          <lucide-icon name="x" [size]="20"></lucide-icon>
        </button>
      </div>

      <div class="chat-messages" #scrollMe [scrollTop]="scrollMe.scrollHeight">
        <div class="welcome-msg" *ngIf="messages().length === 0">
          <lucide-icon name="bot" [size]="40" class="bot-icon-large"></lucide-icon>
          <h4>Namaste! I'm your Hub Assistant.</h4>
          <p>How can I help you with your recharges today?</p>
          <div class="chat-options" style="justify-content: center; margin-top: 16px;">
            <button class="chat-option-btn" (click)="submitOption('Plans & Operators')">Plans & Operators</button>
            <button class="chat-option-btn" (click)="submitOption('Payment Issue')">Payment Issue</button>
            <button class="chat-option-btn" (click)="submitOption('Recharge Issue')">Recharge Issue</button>
            <button class="chat-option-btn" (click)="submitOption('Feedback')">Feedback</button>
          </div>
        </div>

        <div *ngFor="let msg of messages()" [class]="'message-wrapper ' + msg.sender">
          <div class="message-bubble">
            {{ msg.text }}
            <div class="chat-options" *ngIf="msg.options && msg.options.length > 0">
              <button class="chat-option-btn" *ngFor="let opt of msg.options" (click)="submitOption(opt)">
                {{ opt }}
              </button>
            </div>
          </div>
          <span class="time">{{ msg.timestamp | date:'shortTime' }}</span>
        </div>

        <div class="message-wrapper bot" *ngIf="isTyping()">
          <div class="message-bubble typing">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <div class="chat-input">
        <input 
          type="text" 
          [(ngModel)]="userInput" 
          (keyup.enter)="sendMessage()" 
          placeholder="Ask about plans, refunds..."
          [disabled]="isTyping()"
        />
        <button (click)="sendMessage()" [disabled]="!userInput.trim() || isTyping()">
          <lucide-icon name="send" [size]="18"></lucide-icon>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-toggle {
      position: fixed; bottom: 30px; right: 30px;
      width: 64px; height: 64px; border-radius: 50%;
      background: var(--gradient-gold); color: #000;
      border: none; cursor: pointer; z-index: 1000;
      display: flex; align-items: center; justify-content: center;
      box-shadow: 0 8px 32px rgba(225, 202, 150, 0.3);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .chat-toggle:hover { transform: scale(1.1) rotate(5deg); }
    .chat-toggle.hidden { transform: scale(0); opacity: 0; }

    .pulse {
      position: absolute; width: 100%; height: 100%;
      border-radius: 50%; background: var(--accent-gold);
      opacity: 0.5; animation: pulse 2s infinite;
      z-index: -1;
    }
    @keyframes pulse {
      0% { transform: scale(1); opacity: 0.5; }
      100% { transform: scale(1.6); opacity: 0; }
    }

    .chat-window {
      position: fixed; bottom: 100px; right: 30px;
      width: 400px; height: 600px;
      display: flex; flex-direction: column;
      border-radius: 28px; z-index: 9999;
      overflow: hidden; opacity: 0; transform: translateY(20px) scale(0.95);
      pointer-events: none; transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      border: 1px solid rgba(255, 255, 255, 0.1);
      background: rgba(15, 23, 42, 0.9);
      backdrop-filter: blur(20px);
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
    }
    .chat-window.open { opacity: 1; transform: translateY(0) scale(1); pointer-events: all; }

    .chat-header {
      padding: 24px; background: rgba(255, 255, 255, 0.03);
      border-bottom: 1px solid rgba(255, 255, 255, 0.08);
      display: flex; align-items: center; justify-content: space-between;
    }
    .header-info { display: flex; align-items: center; gap: 12px; }
    .bot-avatar {
      width: 36px; height: 36px; border-radius: 12px;
      background: rgba(225, 202, 150, 0.1);
      display: flex; align-items: center; justify-content: center;
      border: 1px solid rgba(225, 202, 150, 0.2);
    }
    .chat-header h3 { font-size: 16px; margin: 0; color: #fff; font-weight: 700; }
    .status { font-size: 11px; color: var(--accent-emerald); font-weight: 600; display: block; }
    .close-btn { background: none; border: none; color: #94a3b8; cursor: pointer; transition: color 0.3s; }
    .close-btn:hover { color: var(--accent-red); }

    .chat-messages {
      flex: 1; overflow-y: auto; padding: 20px;
      display: flex; flex-direction: column; gap: 16px;
      scrollbar-width: thin; scrollbar-color: var(--accent-gold) transparent;
    }
    .welcome-msg { text-align: center; margin-top: 40px; color: #94a3b8; }
    .bot-icon-large { color: var(--accent-gold); margin-bottom: 16px; opacity: 0.5; }
    .welcome-msg h4 { color: #fff; margin-bottom: 8px; }
    .welcome-msg p { font-size: 14px; }

    .message-wrapper { display: flex; flex-direction: column; max-width: 80%; }
    .message-wrapper.user { align-self: flex-end; align-items: flex-end; }
    .message-wrapper.bot { align-self: flex-start; align-items: flex-start; }

    .message-bubble {
      padding: 12px 16px; border-radius: 18px; font-size: 14px; line-height: 1.5;
      position: relative; word-wrap: break-word;
    }
    .user .message-bubble {
      background: var(--gradient-gold); color: #000;
      border-bottom-right-radius: 4px;
    }
    .bot .message-bubble {
      background: rgba(255, 255, 255, 0.08); color: #e2e8f0;
      border-bottom-left-radius: 4px; border: 1px solid var(--glass-border);
    }

    .chat-options {
      display: flex; flex-wrap: wrap; gap: 8px; margin-top: 12px;
    }
    .chat-option-btn {
      background: rgba(225, 202, 150, 0.1); border: 1px solid var(--accent-gold);
      color: var(--accent-gold); padding: 6px 14px; border-radius: 20px;
      font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.2s;
    }
    .chat-option-btn:hover {
      background: var(--accent-gold); color: #000; transform: translateY(-1px);
    }

    .time { font-size: 10px; color: #64748b; margin-top: 4px; font-weight: 500; }

    .chat-input {
      padding: 16px; background: rgba(0,0,0,0.2);
      border-top: 1px solid var(--glass-border);
      display: flex; gap: 10px;
    }
    .chat-input input {
      flex: 1; background: rgba(255,255,255,0.03); border: 1px solid var(--glass-border);
      border-radius: 12px; padding: 12px 16px; color: #fff; font-size: 14px; outline: none;
      transition: all 0.3s;
    }
    .chat-input input:focus { border-color: var(--accent-gold); background: rgba(255,255,255,0.06); }
    .chat-input button {
      width: 44px; height: 44px; border-radius: 12px;
      background: var(--gradient-gold); border: none; color: #000;
      cursor: pointer; display: flex; align-items: center; justify-content: center;
      transition: all 0.3s;
    }
    .chat-input button:disabled { opacity: 0.5; cursor: not-allowed; filter: grayscale(1); }
    .chat-input button:hover:not(:disabled) { transform: scale(1.05); }

    .typing .dot {
      display: inline-block; width: 4px; height: 4px; border-radius: 50%;
      background: #94a3b8; margin: 0 2px; animation: wave 1.3s infinite;
    }
    .typing .dot:nth-child(2) { animation-delay: -1.1s; }
    .typing .dot:nth-child(3) { animation-delay: -0.9s; }
    @keyframes wave {
      0%, 60%, 100% { transform: translateY(0); }
      30% { transform: translateY(-4px); }
    }
  `]
})
export class ChatbotComponent {
  private chatbotSvc = inject(ChatbotService);

  isOpen = signal(false);
  isTyping = signal(false);
  userInput = '';
  messages = signal<Message[]>([]);

  toggleChat() {
    this.isOpen.set(!this.isOpen());
  }

  submitOption(opt: string) {
    this.userInput = opt;
    this.sendMessage();
  }

  sendMessage() {
    if (!this.userInput.trim()) return;

    const question = this.userInput.trim();
    this.userInput = '';

    const userMsg: Message = {
      text: question,
      sender: 'user',
      timestamp: new Date()
    };
    this.messages.update(prev => [...prev, userMsg]);

    const lowerQ = question.toLowerCase();

    let matchedFlow = STATIC_FLOWS[lowerQ];

    // Fuzzy matching for plans and operators
    if (!matchedFlow) {
      if (lowerQ.includes('jio') && (lowerQ.includes('plan') || lowerQ.includes('operator'))) {
        matchedFlow = STATIC_FLOWS['jio plans'];
      } else if (lowerQ.includes('airtel') && (lowerQ.includes('plan') || lowerQ.includes('operator'))) {
        matchedFlow = STATIC_FLOWS['airtel plans'];
      } else if ((lowerQ.includes('vi') || lowerQ.includes('vodafone')) && (lowerQ.includes('plan') || lowerQ.includes('operator'))) {
        matchedFlow = STATIC_FLOWS['vi plans'];
      } else if (lowerQ.includes('bsnl') && (lowerQ.includes('plan') || lowerQ.includes('operator'))) {
        matchedFlow = STATIC_FLOWS['bsnl plans'];
      } else if (lowerQ.includes('plan') || lowerQ.includes('operator')) {
        matchedFlow = STATIC_FLOWS['plans & operators'];
      }
    }

    if (matchedFlow) {
      setTimeout(() => {
        const botMsg: Message = { 
          text: matchedFlow.text, 
          sender: 'bot', 
          timestamp: new Date(), 
          options: matchedFlow.options 
        };
        this.messages.update(prev => [...prev, botMsg]);
      }, 400);
      return;
    }

    this.isTyping.set(true);

    this.chatbotSvc.ask(question).subscribe({
      next: (res: any) => {
        this.isTyping.set(false);
        const botMsg: Message = {
          text: res.answer,
          sender: 'bot',
          timestamp: new Date(),
          options: ["Main Menu"]
        };
        this.messages.update(prev => [...prev, botMsg]);
      },
      error: () => {
        this.isTyping.set(false);
        const errorMsg: Message = {
          text: "Thank you! I've noted that down.",
          sender: 'bot',
          timestamp: new Date(),
          options: ["Main Menu"]
        };
        this.messages.update(prev => [...prev, errorMsg]);
      }
    });
  }
}
