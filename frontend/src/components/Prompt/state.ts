import { FormSchema } from '@/components/Form';

type InputType = 'InputTextArea' | 'InputNumber' | 'Input';
export interface PromptProps {
  title: string;
  label?: string;
  required?: boolean;
  onOK?: Fn;
  inputType?: InputType;
  labelWidth?: number;
  width?: string;
  layout?: 'horizontal' | 'vertical' | 'inline';
  defaultValue?: string | number;
}

interface genFormSchemasProps {
  label?: string;
  required?: boolean;
  inputType?: InputType;
  defaultValue?: string | number;
}

const inputTypeMap: {
  [key in InputType]: {
    colProps: { span: number; offset?: number };
    componentProps: FormSchema['componentProps'];
  };
} = {
  InputTextArea: {
    colProps: { span: 23 },
    componentProps: {
      placeholder: 'Vui lòng nhập nội dung',
      autoSize: { minRows: 2, maxRows: 6 },
      maxlength: 255,
      showCount: true,
    },
  },
  InputNumber: {
    colProps: { span: 20, offset: 2 },
    componentProps: {
      placeholder: 'Vui lòng nhập số',
      min: 0,
    },
  },
  Input: {
    colProps: { span: 20, offset: 2 },
    componentProps: {
      placeholder: 'Vui lòng nhập nội dung',
      min: 0,
    },
  },
};

export function genFormSchemas({
  label = 'Ghi chú',
  required = true,
  inputType = 'InputTextArea',
  defaultValue = '',
}: genFormSchemasProps) {
  const formSchema: FormSchema = {
    field: 'txt',
    component: inputType,
    label,
    defaultValue,
    required: Boolean(required),
    ...inputTypeMap[inputType],
  };
  return [formSchema];
}
