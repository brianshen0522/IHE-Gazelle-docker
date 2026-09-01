import UserActivation from "../../components/registration/UserActivation";

const Activation = async ({ params }: { params: Promise<{ activationCode: string }> }) => {
  const { activationCode } = await params;
  return <UserActivation activationCode={activationCode} />;
};

export default Activation;
