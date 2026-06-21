export const mockUsers = [
  {
    id: 101,
    name: "Kuba (Ty)",
    email: "kuba@example.com",
    avatar: "KJ",
    color: "bg-success",
    joinedEvents: [1, 2],
  },
  {
    id: 102,
    name: "Ania Kowalska",
    email: "ania@example.com",
    avatar: "AK",
    color: "bg-light border text-muted",
    joinedEvents: [1],
  },
  {
    id: 103,
    name: "Marek Nowak",
    email: "marek@example.com",
    avatar: "MN",
    color: "bg-info",
    joinedEvents: [1, 2],
  },
];

export const allEvents = [
  {
    id: 1,
    title: "Narty we Włoszech",
    daysLeft: 3,
    goal: 2500,
    collected: 1850,
    expenses: [
      {
        id: 1,
        userId: 102,
        title: "Chleb i dodatki",
        amount: -10.0,
        date: "2026-05-04",
      },
      {
        id: 2,
        userId: 101,
        title: "Parking pod stokiem",
        amount: -50.0,
        date: "2026-05-05",
      },
    ],
    tasks: [
      { id: 1, title: "Grill gazowy", assignedTo: "M", isDone: true },
      { id: 2, title: "Głośnik Bluetooth", assignedTo: null, isDone: false },
      { id: 3, title: "Apteczka", assignedTo: "A", isDone: false },
    ],
    participants: [
      { userId: 101, role: "Organizator", status: "Potwierdził" },
      { userId: 102, role: "Uczestnik", status: "Potwierdziła" },
      { userId: 103, role: "Uczestnik", status: "W oczekiwaniu" },
    ],
  },
  {
    id: 2,
    title: "Majówka na Mazurach",
    daysLeft: 20,
    goal: 1000,
    collected: 200,
    expenses: [],
    tasks: [{ id: 1, title: "Kupić węgiel", assignedTo: null, isDone: false }],
    participants: [
      { userId: 101, role: "Organizator", status: "Potwierdził" },
      { userId: 103, role: "Uczestnik", status: "Potwierdził" },
    ],
  },
];
