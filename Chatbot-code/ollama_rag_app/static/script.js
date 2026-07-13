async function refreshDocuments() {
  const res = await fetch('/documents');
  const docs = await res.json();
  const docList = document.getElementById('docList');

  if (!docs.length) {
    docList.innerHTML = '<p class="muted">No documents uploaded yet.</p>';
    return;
  }

  docList.innerHTML = docs.map(doc => `
    <div class="doc-item">
      <div>${escapeHtml(doc.filename)}</div>
      <small>${doc.chunk_count} chunks</small>
    </div>
  `).join('');
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.innerText = text;
  return div.innerHTML;
}

const staticFlows = {
  "hi": { text: "Hello! How can I help you today?", options: ["Payment Issue", "Recharge Issue", "General Inquiry", "Feedback"] },
  "hello": { text: "Hello! How can I help you today?", options: ["Payment Issue", "Recharge Issue", "General Inquiry", "Feedback"] },
  "main menu": { text: "Main Menu. How can I help you?", options: ["Payment Issue", "Recharge Issue", "General Inquiry", "Feedback"] },
  
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

window.submitOption = function(opt) {
  handleUserSubmission(opt);
};

function addMessage(role, text, sources = [], options = []) {
  const chatBox = document.getElementById('chatBox');
  const wrapper = document.createElement('div');
  wrapper.className = `message ${role}`;

  const bubble = document.createElement('div');
  bubble.className = 'bubble';
  bubble.textContent = text;

  if (role === 'bot' && sources.length) {
    const sourcesDiv = document.createElement('div');
    sourcesDiv.className = 'sources';
    const title = document.createElement('div');
    title.innerHTML = '<strong>Retrieved context</strong>';
    sourcesDiv.appendChild(title);

    sources.forEach(src => {
      const item = document.createElement('div');
      item.textContent = `${src.filename} | chunk ${src.chunk_no} | score ${src.score}`;
      sourcesDiv.appendChild(item);
    });

    bubble.appendChild(sourcesDiv);
  }

  if (options && options.length > 0) {
    const optionsDiv = document.createElement('div');
    optionsDiv.className = 'chat-options';
    options.forEach(opt => {
      const btn = document.createElement('button');
      btn.className = 'chat-option-btn';
      btn.textContent = opt;
      btn.onclick = () => submitOption(opt);
      optionsDiv.appendChild(btn);
    });
    bubble.appendChild(optionsDiv);
  }

  wrapper.appendChild(bubble);
  chatBox.appendChild(wrapper);
  chatBox.scrollTop = chatBox.scrollHeight;
}

document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const fileInput = document.getElementById('fileInput');
  const uploadStatus = document.getElementById('uploadStatus');

  if (!fileInput.files.length) {
    uploadStatus.textContent = 'Please choose a file.';
    return;
  }

  const formData = new FormData();
  formData.append('file', fileInput.files[0]);
  uploadStatus.textContent = 'Uploading and indexing...';

  try {
    const res = await fetch('/upload', { method: 'POST', body: formData });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || 'Upload failed');
    uploadStatus.textContent = `${data.message} (${data.chunks} chunks)`;
    fileInput.value = '';
    await refreshDocuments();
  } catch (err) {
    uploadStatus.textContent = err.message;
  }
});

async function handleUserSubmission(question) {
  if (!question) return;

  addMessage('user', question);
  const lowerQ = question.toLowerCase().trim();

  if (staticFlows[lowerQ]) {
    const flow = staticFlows[lowerQ];
    addMessage('bot', flow.text, [], flow.options);
    return;
  }

  addMessage('bot', 'Thinking...');

  try {
    const res = await fetch('/ask', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question })
    });
    const data = await res.json();

    const chatBox = document.getElementById('chatBox');
    chatBox.removeChild(chatBox.lastElementChild);

    if (!res.ok) throw new Error(data.error || 'Request failed');
    addMessage('bot', data.answer, data.sources || [], ["Main Menu"]);
  } catch (err) {
    const chatBox = document.getElementById('chatBox');
    chatBox.removeChild(chatBox.lastElementChild);
    addMessage('bot', `Thank you! I've noted that down. (Or error: ${err.message})`, [], ["Main Menu"]);
  }
}

document.getElementById('chatForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const input = document.getElementById('questionInput');
  const question = input.value.trim();
  input.value = '';
  await handleUserSubmission(question);
});
