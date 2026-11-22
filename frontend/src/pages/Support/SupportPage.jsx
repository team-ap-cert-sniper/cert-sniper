import NoticeSection from '@/pages/Support/components/NoticeSection';
import FaqSection from '@/pages/Support/components/FaqSection';
import InquiryFormSection from '@/pages/Support/components/InquiryFormSection';
import MyInquirySection from '@/pages/Support/components/MyInquirySection';

export default function SupportPage() {
  return (
    <section>
      <h1>고객센터</h1>
      <NoticeSection />
      <FaqSection />
      <InquiryFormSection />
      <MyInquirySection />
    </section>
  );
}
